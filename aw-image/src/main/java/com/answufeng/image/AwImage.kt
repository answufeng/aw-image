package com.answufeng.image

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.Disposable
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.ConcurrentHashMap

@DslMarker
annotation class AwImageDsl

/**
 * aw-image 图片加载库全局配置入口。
 *
 * 通过 [init] 方法初始化全局 ImageLoader，设置缓存策略、占位图、GIF 支持等。
 * 未调用 [init] 时，Coil 将使用默认 ImageLoader。
 *
 * ```kotlin
 * class App : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         AwImage.init(this) {
 *             memoryCacheSize(0.25)
 *             diskCacheSize(256L * 1024 * 1024)
 *             enableGif(true)
 *             placeholder(R.drawable.placeholder)
 *             placeholder(ColorDrawable(Color.GRAY))
 *             error(R.drawable.error)
 *             okHttpClient(myCustomClient)
 *             enableLogging(BuildConfig.DEBUG)
 *         }
 *     }
 * }
 * ```
 */
object AwImage {

    /** 全局占位图资源 ID，[loadImage][com.answufeng.image.loadImage] 未指定时使用 */
    @Volatile
    internal var globalPlaceholder: Int = 0
        private set

    /** 全局占位图 Drawable，优先级高于 [globalPlaceholder] */
    @Volatile
    internal var globalPlaceholderDrawable: Drawable? = null
        private set

    /** 全局错误图资源 ID，[loadImage][com.answufeng.image.loadImage] 未指定时使用 */
    @Volatile
    internal var globalError: Int = 0
        private set

    /** 全局错误图 Drawable，优先级高于 [globalError] */
    @Volatile
    internal var globalErrorDrawable: Drawable? = null
        private set

    /** 全局兜底图资源 ID，data 为 null 时使用 */
    @Volatile
    internal var globalFallback: Int = 0
        private set

    /** 全局兜底图 Drawable，优先级高于 [globalFallback] */
    @Volatile
    internal var globalFallbackDrawable: Drawable? = null
        private set

    /** 全局是否启用渐入动画 */
    @Volatile
    internal var globalCrossfadeEnabled: Boolean = true
        private set

    /** 全局渐入动画时长（ms） */
    @Volatile
    internal var globalCrossfadeDuration: Int = 200
        private set

    @Volatile
    private var initialized = false

    /** 是否已调用 [init] */
    val isInitialized: Boolean get() = initialized

    private val taggedDisposables = ConcurrentHashMap<Any, MutableList<Disposable>>()

    /**
     * 初始化全局 ImageLoader。
     *
     * 建议在 [Application.onCreate] 中调用。多次调用会覆盖之前的配置。
     *
     * @param context 任意 Context，内部会转为 ApplicationContext
     * @param config  可选的 DSL 配置块
     * @return 创建的 [ImageLoader] 实例，方便高级用户进一步定制
     */
    fun init(context: Context, config: (ImageConfig.() -> Unit)? = null): ImageLoader {
        val appContext = context.applicationContext
        val imageConfig = ImageConfig().apply { config?.invoke(this) }

        AwLogger.d("AwImage.init: memoryCache=${imageConfig.memoryCachePercent}, " +
                "diskCache=${imageConfig.diskCacheSize}, gif=${imageConfig.gifEnabled}")

        val builder = ImageLoader.Builder(appContext)

        if (imageConfig.crossfadeEnabled) {
            builder.crossfade(imageConfig.crossfadeDuration)
        }

        builder.memoryCache {
            val memBuilder = MemoryCache.Builder(appContext)
            val maxBytes = imageConfig.memoryCacheMaxBytes
            if (maxBytes != null) {
                memBuilder.maxSizeBytes(maxBytes.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            } else {
                memBuilder.maxSizePercent(imageConfig.memoryCachePercent)
            }
            memBuilder.build()
        }

        builder.diskCache {
            DiskCache.Builder()
                .directory(imageConfig.diskCacheDir ?: appContext.cacheDir.resolve("aw_image_cache"))
                .maxSizeBytes(imageConfig.diskCacheSize)
                .build()
        }

        if (imageConfig.gifEnabled) {
            builder.components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
        }

        if (imageConfig.svgEnabled) {
            builder.components {
                add(coil.decode.SvgDecoder.Factory())
            }
        }

        imageConfig.okHttpClient?.let { client ->
            val enhancedClient = client.newBuilder()
                .addInterceptor(ProgressInterceptor)
                .build()
            builder.okHttpClient(enhancedClient)
        } ?: run {
            builder.okHttpClient(
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor(ProgressInterceptor)
                    .build()
            )
        }

        globalPlaceholder = imageConfig.placeholderRes
        globalPlaceholderDrawable = imageConfig.placeholderDrawable?.constantState?.newDrawable()?.mutate()
        globalError = imageConfig.errorRes
        globalErrorDrawable = imageConfig.errorDrawable?.constantState?.newDrawable()?.mutate()
        globalFallback = imageConfig.fallbackRes
        globalFallbackDrawable = imageConfig.fallbackDrawable?.constantState?.newDrawable()?.mutate()
        globalCrossfadeEnabled = imageConfig.crossfadeEnabled
        globalCrossfadeDuration = imageConfig.crossfadeDuration

        val imageLoader = builder.build()
        Coil.setImageLoader(imageLoader)
        initialized = true
        AwLogger.d("AwImage.init: complete")
        return imageLoader
    }

    /** 获取当前 ImageLoader 实例 */
    fun imageLoader(context: Context): ImageLoader = Coil.imageLoader(context)

    /**
     * 清除内存缓存。
     *
     * 内置异常保护，操作失败不会崩溃。
     *
     * @return `true` 表示清除成功，`false` 表示失败
     */
    fun clearMemoryCache(context: Context): Boolean {
        return runCatching {
            imageLoader(context).memoryCache?.clear()
            AwLogger.d("clearMemoryCache: success")
        }.onFailure {
            AwLogger.e("clearMemoryCache: failed", it)
        }.isSuccess
    }

    /**
     * 清除磁盘缓存。
     *
     * 内置异常保护，操作失败不会崩溃。
     *
     * @return `true` 表示清除成功，`false` 表示失败
     */
    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun clearDiskCache(context: Context): Boolean {
        return runCatching {
            imageLoader(context).diskCache?.clear()
            AwLogger.d("clearDiskCache: success")
        }.onFailure {
            AwLogger.e("clearDiskCache: failed", it)
        }.isSuccess
    }

    /**
     * 获取内存缓存当前占用字节数。
     *
     * @return 缓存字节数，未初始化或获取失败返回 0
     */
    fun getMemoryCacheSize(context: Context): Long {
        return runCatching {
            imageLoader(context).memoryCache?.size?.toLong() ?: 0L
        }.onFailure {
            AwLogger.e("getMemoryCacheSize: failed", it)
        }.getOrDefault(0L)
    }

    /**
     * 获取磁盘缓存当前占用字节数。
     *
     * @return 缓存字节数，未初始化或获取失败返回 0
     */
    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun getDiskCacheSize(context: Context): Long {
        return runCatching {
            imageLoader(context).diskCache?.size ?: 0L
        }.onFailure {
            AwLogger.e("getDiskCacheSize: failed", it)
        }.getOrDefault(0L)
    }

    /**
     * 检查指定数据源是否已缓存。
     *
     * 同时检查内存缓存和磁盘缓存。
     *
     * @param context Context
     * @param data    图片数据源（URL / File / @DrawableRes 等）
     * @return `true` 表示已缓存，`false` 表示未缓存或查询失败
     */
    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    fun isCached(context: Context, data: Any): Boolean {
        return runCatching {
            val loader = imageLoader(context)
            val memoryKey = MemoryCache.Key(data, emptyList())
            loader.memoryCache?.get(memoryKey) != null
        }.onFailure {
            AwLogger.e("isCached: failed for data=$data", it)
        }.getOrDefault(false)
    }

    /**
     * 取消指定标签的所有图片加载请求。
     *
     * ```kotlin
     * // 加载时设置标签
     * imageView.loadImage(url) { tag("feed_list") }
     *
     * // 退出页面时批量取消
     * AwImage.cancelByTag("feed_list")
     * ```
     *
     * @param tag 请求标签
     */
    fun cancelByTag(tag: Any) {
        val disposables = taggedDisposables.remove(tag) ?: return
        for (d in disposables) {
            if (!d.isDisposed) d.dispose()
        }
        AwLogger.d("cancelByTag: cancelled ${disposables.size} requests for tag=$tag")
    }

    internal fun registerTaggedDisposable(tag: Any, disposable: Disposable) {
        val list = taggedDisposables.getOrPut(tag) { mutableListOf() }
        list.add(disposable)
        disposable.job.invokeOnCompletion { _ ->
            list.remove(disposable)
            if (list.isEmpty()) {
                taggedDisposables.remove(tag, list)
            }
        }
    }

    /**
     * 全局配置 DSL 类。
     *
     * 所有属性通过 setter 方法设置，外部不可直接赋值。
     */
    @AwImageDsl
    class ImageConfig {
        /** 内存缓存占应用可用内存的比例（0.05~0.5） */
        var memoryCachePercent: Double = 0.25
            private set

        @Suppress("ktlint")
        internal var memoryCacheMaxBytes: Long? = null
            private set

        /** 磁盘缓存最大字节数 */
        var diskCacheSize: Long = 100L * 1024 * 1024
            private set

        @Suppress("ktlint")
        internal var diskCacheDir: File? = null
            private set

        /** 是否启用全局渐入动画 */
        var crossfadeEnabled: Boolean = true
            private set

        /** 全局渐入动画时长（ms） */
        var crossfadeDuration: Int = 200
            private set

        /** 是否启用 GIF 解码 */
        var gifEnabled: Boolean = true
            private set

        /** 是否启用 SVG 解码 */
        var svgEnabled: Boolean = false
            private set

        /** 全局占位图资源 ID */
        var placeholderRes: Int = 0
            private set

        @Suppress("ktlint")
        internal var placeholderDrawable: Drawable? = null
            private set

        /** 全局错误图资源 ID */
        var errorRes: Int = 0
            private set

        @Suppress("ktlint")
        internal var errorDrawable: Drawable? = null
            private set

        /** 全局兜底图资源 ID */
        var fallbackRes: Int = 0
            private set

        @Suppress("ktlint")
        internal var fallbackDrawable: Drawable? = null
            private set

        @Suppress("ktlint")
        internal var okHttpClient: OkHttpClient? = null
            private set

        /** 按比例设置内存缓存大小（0.05~0.5） */
        fun memoryCacheSize(percent: Double) { memoryCachePercent = percent.coerceIn(0.05, 0.5) }

        /** 按字节数设置内存缓存大小（优先级高于 [memoryCacheSize]） */
        fun memoryCacheMaxSize(bytes: Long) {
            memoryCacheMaxBytes = bytes.coerceAtLeast(0)
        }

        /** 设置磁盘缓存最大字节数 */
        fun diskCacheSize(bytes: Long) { diskCacheSize = bytes.coerceAtLeast(0) }

        /** 设置磁盘缓存目录（默认 `{cacheDir}/aw_image_cache`） */
        fun diskCacheDir(directory: File) { diskCacheDir = directory }

        /** 设置是否启用全局渐入动画 */
        fun crossfade(enabled: Boolean) { crossfadeEnabled = enabled }

        /** 设置全局渐入动画时长（ms），同时自动启用渐入动画 */
        fun crossfade(durationMs: Int) {
            crossfadeDuration = durationMs.coerceAtLeast(0)
            if (durationMs > 0) crossfadeEnabled = true
        }

        /** 设置是否启用 GIF 解码 */
        fun enableGif(enabled: Boolean) { gifEnabled = enabled }

        /** 设置是否启用 SVG 解码（默认 false） */
        fun enableSvg(enabled: Boolean) { svgEnabled = enabled }

        /** 设置全局占位图资源 ID */
        fun placeholder(res: Int) { placeholderRes = res }

        /** 设置全局占位图 Drawable（优先级高于资源 ID） */
        fun placeholder(drawable: Drawable) { placeholderDrawable = drawable }

        /** 设置全局错误图资源 ID */
        fun error(res: Int) { errorRes = res }

        /** 设置全局错误图 Drawable（优先级高于资源 ID） */
        fun error(drawable: Drawable) { errorDrawable = drawable }

        /** 设置全局兜底图资源 ID（data 为 null 时显示） */
        fun fallback(res: Int) { fallbackRes = res }

        /** 设置全局兜底图 Drawable（data 为 null 时显示，优先级高于资源 ID） */
        fun fallback(drawable: Drawable) { fallbackDrawable = drawable }

        /** 设置自定义 OkHttpClient（用于自定义超时、拦截器等） */
        fun okHttpClient(client: OkHttpClient) { okHttpClient = client }

        /** 设置是否启用调试日志（默认 false） */
        fun enableLogging(enabled: Boolean) { AwLogger.enabled = enabled }
    }
}
