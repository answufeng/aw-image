package com.answufeng.image

import android.graphics.drawable.Drawable
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext

/**
 * 图片预加载器。
 *
 * 支持单张/批量预加载和获取已缓存的 [Drawable]。
 * 批量预加载通过 [Semaphore] 控制并发数，避免瞬间发起大量请求。
 *
 * 线程约束：所有 `suspend` 方法内部切换到 [kotlinx.coroutines.Dispatchers.IO]，
 * 因此可在任意 dispatcher 调用。建议在 ViewModel 或 lifecycleScope 中调用。
 *
 * ```kotlin
 * lifecycleScope.launch {
 *     ImagePreloader.preload(context, url) { override(200, 200) }
 *     ImagePreloader.preloadAll(context, urls, concurrency = 8) { override(200, 200) }
 *     val drawable = ImagePreloader.getDrawable(context, url) { override(200, 200) }
 * }
 * ```
 *
 * [config] 与 [loadImage] 使用相同的 [AwImageScope] DSL（如 [AwImageScope.override]），
 * 以保证与列表展示命中同一缓存。
 */
object ImagePreloader {
    /**
     * 预加载单张图片到缓存。
     *
     * @param config 与 [loadImage] 相同的 [AwImageScope] 配置（如 [AwImageScope.override]、变换等）
     * @return `true` 表示加载成功并已缓存，`false` 表示失败
     */
    @JvmSynthetic
    suspend fun preload(
        context: android.content.Context,
        data: Any,
        config: (AwImageScope.() -> Unit)? = null,
    ): Boolean {
        val appContext = context.applicationContext
        return withContext(Dispatchers.IO) {
            runCatching {
                val builder = ImageRequest.Builder(appContext).data(data)
                builder.applyAwImageScope(appContext, data, config)
                val request = builder.build()
                val result = Coil.imageLoader(appContext).execute(request)
                val success = result is SuccessResult
                AwImageLogger.d("preload: data=$data, success=$success")
                success
            }.onFailure {
                AwImageLogger.e("preload: failed for data=$data", it)
            }.getOrDefault(false)
        }
    }

    /**
     * 获取图片 [Drawable]；**未命中缓存时会发起网络/磁盘加载**。
     *
     * @param config 与 [loadImage] 相同的 [AwImageScope] 配置
     * @return 成功时返回 [Drawable]，失败返回 null
     */
    @JvmSynthetic
    suspend fun getDrawable(
        context: android.content.Context,
        data: Any,
        config: (AwImageScope.() -> Unit)? = null,
    ): Drawable? {
        val appContext = context.applicationContext
        return withContext(Dispatchers.IO) {
            runCatching {
                val builder =
                    ImageRequest.Builder(appContext)
                        .data(data)
                        .allowHardware(false)
                builder.applyAwImageScope(appContext, data, config)
                val request = builder.build()
                val result = Coil.imageLoader(appContext).execute(request)
                (result as? SuccessResult)?.drawable
            }.onFailure {
                AwImageLogger.e("getDrawable: failed for data=$data", it)
            }.getOrNull()
        }
    }

    /**
     * 批量预加载图片。
     *
     * @param concurrency 最大并发数，默认 8
     * @param config 应用到每一张预加载请求的配置
     * @return 每个数据源的加载结果列表（`true` = 成功）
     */
    @JvmSynthetic
    suspend fun preloadAll(
        context: android.content.Context,
        urls: List<Any>,
        concurrency: Int = 8,
        config: (AwImageScope.() -> Unit)? = null,
    ): List<Boolean> {
        require(concurrency >= 1) { "concurrency must be >= 1, got $concurrency" }
        val appContext = context.applicationContext
        AwImageLogger.d("preloadAll: ${urls.size} URLs, concurrency=$concurrency")
        val semaphore = Semaphore(concurrency)
        return coroutineScope {
            urls.map { url ->
                async {
                    semaphore.acquire()
                    try {
                        preload(appContext, url, config)
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }
    }
}
