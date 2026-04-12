package com.answufeng.image

import android.content.Context
import android.os.Build
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import java.io.File

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
 *             error(R.drawable.error)
 *             enableLogging(BuildConfig.DEBUG)
 *         }
 *     }
 * }
 * ```
 */
object AwImage {

    /** 全局占位图资源 ID，[loadImage][com.answufeng.image.loadImage] 未指定时使用 */
    internal var globalPlaceholder: Int = 0
        private set

    /** 全局错误图资源 ID，[loadImage][com.answufeng.image.loadImage] 未指定时使用 */
    internal var globalError: Int = 0
        private set

    private var initialized = false

    /** 是否已调用 [init] */
    val isInitialized: Boolean get() = initialized

    /**
     * 初始化全局 ImageLoader。
     *
     * 建议在 [Application.onCreate] 中调用。多次调用会覆盖之前的配置。
     *
     * @param context 任意 Context，内部会转为 ApplicationContext
     * @param config  可选的 DSL 配置块
     */
    fun init(context: Context, config: (ImageConfig.() -> Unit)? = null) {
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
                memBuilder.maxSizeBytes(maxBytes.toInt())
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

        globalPlaceholder = imageConfig.placeholderRes
        globalError = imageConfig.errorRes

        Coil.setImageLoader(builder.build())
        initialized = true
        AwLogger.d("AwImage.init: complete")
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
     * 全局配置 DSL 类。
     *
     * 所有属性通过 setter 方法设置，外部不可直接赋值。
     */
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

        /** 全局占位图资源 ID */
        var placeholderRes: Int = 0
            private set

        /** 全局错误图资源 ID */
        var errorRes: Int = 0
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

        /** 设置全局占位图 */
        fun placeholder(res: Int) { placeholderRes = res }

        /** 设置全局错误图 */
        fun error(res: Int) { errorRes = res }

        /** 设置是否启用调试日志（默认 false） */
        fun enableLogging(enabled: Boolean) { AwLogger.enabled = enabled }
    }
}
