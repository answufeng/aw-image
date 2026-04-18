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
 * ```kotlin
 * lifecycleScope.launch {
 *     // 单张预加载
 *     val success: Boolean = ImagePreloader.preload(context, url)
 *
 *     // 批量预加载（返回每个 URL 的加载结果）
 *     val results: List<Boolean> = ImagePreloader.preloadAll(context, urls, concurrency = 8)
 *
 *     // 获取已缓存的 Drawable
 *     val drawable: Drawable? = ImagePreloader.get(context, url)
 * }
 * ```
 */
object ImagePreloader {

    /**
     * 预加载单张图片到缓存。
     *
     * @param context Context
     * @param data    图片数据源（URL / File / @DrawableRes 等）
     * @return `true` 表示加载成功并已缓存，`false` 表示失败
     */
    suspend fun preload(context: android.content.Context, data: Any): Boolean {
        val appContext = context.applicationContext
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(appContext)
                    .data(data)
                    .build()
                val result = Coil.imageLoader(appContext).execute(request)
                val success = result is SuccessResult
                AwLogger.d("preload: data=$data, success=$success")
                success
            }.onFailure {
                AwLogger.e("preload: failed for data=$data", it)
            }.getOrDefault(false)
        }
    }

    /**
     * 获取已缓存的图片 [Drawable]。
     *
     * 与 [preload] 不同，此方法返回 [Drawable] 对象，可直接设置到 ImageView。
     * 如果未命中缓存，会触发加载；加载失败返回 null。
     *
     * 内部禁用硬件 Bitmap（`allowHardware(false)`），确保返回的 Drawable 可直接使用。
     *
     * @param context Context
     * @param data    图片数据源
     * @return 已缓存的 [Drawable]，未命中缓存或加载失败时返回 null
     */
    suspend fun get(context: android.content.Context, data: Any): Drawable? {
        val appContext = context.applicationContext
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(appContext)
                    .data(data)
                    .allowHardware(false)
                    .build()
                val result = Coil.imageLoader(appContext).execute(request)
                (result as? SuccessResult)?.drawable
            }.onFailure {
                AwLogger.e("get: failed for data=$data", it)
            }.getOrNull()
        }
    }

    /**
     * 批量预加载图片。
     *
     * 使用 [Semaphore] 控制并发数，避免瞬间发起大量网络请求。
     * 返回 `List<Boolean>` 表示每个 URL 的加载结果。
     *
     * @param context     Context
     * @param urls        图片数据源列表
     * @param concurrency 最大并发数，默认 8
     * @return 每个数据源的加载结果列表（`true` = 成功）
     * @throws IllegalArgumentException 如果 [concurrency] < 1
     */
    suspend fun preloadAll(
        context: android.content.Context,
        urls: List<Any>,
        concurrency: Int = 8
    ): List<Boolean> {
        require(concurrency >= 1) { "concurrency must be >= 1, got $concurrency" }
        val appContext = context.applicationContext
        AwLogger.d("preloadAll: ${urls.size} URLs, concurrency=$concurrency")
        val semaphore = Semaphore(concurrency)
        return coroutineScope {
            urls.map { url ->
                async {
                    semaphore.acquire()
                    try {
                        preload(appContext, url)
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }
    }
}
