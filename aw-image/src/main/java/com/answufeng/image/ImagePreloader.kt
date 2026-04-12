package com.answufeng.image

import android.content.Context
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
 * 图片预加载器——在后台提前加载图片到缓存，后续显示时直接读取。
 *
 * ```kotlin
 * // 预加载单张
 * val success = ImagePreloader.preload(context, url)
 *
 * // 批量预加载（并发可控）
 * ImagePreloader.preloadAll(context, urls, concurrency = 8)
 *
 * // 获取已缓存的 Drawable
 * val drawable = ImagePreloader.get(context, url)
 * ```
 */
object ImagePreloader {

    /**
     * 预加载单张图片到磁盘/内存缓存。
     *
     * 在 IO 线程执行，内置异常保护。
     *
     * @param context Context
     * @param data    图片数据源（URL / File / @DrawableRes 等）
     * @return `true` 加载成功，`false` 加载失败或异常
     */
    suspend fun preload(context: Context, data: Any): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(data)
                    .build()
                val result = Coil.imageLoader(context).execute(request)
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
     * 如果图片未缓存，会同步加载。在 IO 线程执行。
     *
     * @param context Context
     * @param data    图片数据源
     * @return [Drawable]，加载失败返回 null
     */
    suspend fun get(context: Context, data: Any): Drawable? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val request = ImageRequest.Builder(context)
                    .data(data)
                    .allowHardware(false)
                    .build()
                val result = Coil.imageLoader(context).execute(request)
                (result as? SuccessResult)?.drawable
            }.onFailure {
                AwLogger.e("get: failed for data=$data", it)
            }.getOrNull()
        }
    }

    /**
     * 批量预加载图片，使用 [Semaphore] 控制并发数。
     *
     * ```kotlin
     * lifecycleScope.launch {
     *     ImagePreloader.preloadAll(context, urls, concurrency = 8)
     * }
     * ```
     *
     * @param context     Context
     * @param urls        图片数据源列表
     * @param concurrency 最大并发数（默认 8，必须 >= 1）
     * @throws IllegalArgumentException 如果 [concurrency] < 1
     */
    suspend fun preloadAll(context: Context, urls: List<Any>, concurrency: Int = 8) {
        require(concurrency >= 1) { "concurrency must be >= 1, got $concurrency" }
        AwLogger.d("preloadAll: ${urls.size} URLs, concurrency=$concurrency")
        val semaphore = Semaphore(concurrency)
        coroutineScope {
            urls.map { url ->
                async {
                    semaphore.acquire()
                    try {
                        runCatching { preload(context, url) }
                    } finally {
                        semaphore.release()
                    }
                }
            }.awaitAll()
        }
        AwLogger.d("preloadAll: complete")
    }
}
