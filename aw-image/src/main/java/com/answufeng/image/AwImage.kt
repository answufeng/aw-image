package com.answufeng.image

import android.content.ComponentCallbacks2
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
import coil.request.ImageRequest
import okhttp3.OkHttpClient
import kotlin.jvm.JvmOverloads
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

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

    /**
     * 全局占位图资源 ID，[loadImage][com.answufeng.image.loadImage] 未指定时使用。
     * 在 [init] 时从 [ImageConfig] 写入，多线程**读安全**；勿在业务代码中直接修改。
     */
    @Volatile
    internal var globalPlaceholder: Int = 0
        private set

    /**
     * 全局占位 [Drawable]（[init] 时从 [ImageConfig] 中 `mutate` 的副本），优先级高于 [globalPlaceholder]。
     * **只读使用**：不要在多线程下改变其状态（`setColorFilter` 等），以免与其他页面交叉影响。
     */
    @Volatile
    internal var globalPlaceholderDrawable: Drawable? = null
        private set

    /** 全局错误图资源 ID。语义同 [globalPlaceholder]（只读、勿直接改）。 */
    @Volatile
    internal var globalError: Int = 0
        private set

    /**
     * 全局错误图 Drawable（[init] 中 mutate 副本），优先级高于 [globalError]；**只读**使用。
     */
    @Volatile
    internal var globalErrorDrawable: Drawable? = null
        private set

    /** 全局兜底图资源 ID，data 为 null 时使用。只读。 */
    @Volatile
    internal var globalFallback: Int = 0
        private set

    /**
     * 全局兜底图 Drawable，优先级高于 [globalFallback]；**只读**使用。
     */
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

    /**
     * [init] 中 [ImageConfig.defaultRequestListener] 设置的与单次 DSL 监听器**合并**的全局回拨（先本监听器、再 per-request）。
     * 勿在业务中直接改写；应通过 [ImageConfig.defaultRequestListener] 配置。
     */
    @Volatile
    internal var globalRequestListener: ImageRequest.Listener? = null
        private set

    /** 是否已调用 [init] */
    val isInitialized: Boolean get() = initialized

    private val taggedDisposables = ConcurrentHashMap<Any, CopyOnWriteArrayList<Disposable>>()

    /**
     * 初始化全局 ImageLoader。
     *
     * 建议在 [Application.onCreate] 中调用。多次调用会覆盖之前的配置，
     * 包括已设置的 ImageLoader、占位图/错误图/兜底图、全局 crossfade、
     * [ImageConfig.defaultRequestListener] 等。若只改部分项，也需在块内写全量意图（未调用的将恢复为默认）。
     *
     * @param context 任意 Context，内部会转为 ApplicationContext
     * @param config  可选的 DSL 配置块
     * @return 创建的 [ImageLoader] 实例，方便高级用户进一步定制
     */
    fun init(context: Context, config: (ImageConfig.() -> Unit)? = null): ImageLoader = synchronized(AwImage) {
        AwImageLogger.resetForInit()
        val appContext = context.applicationContext
        val imageConfig = ImageConfig().apply { config?.invoke(this) }

        ImageNetworkMonitor.isStrictNetworkForOffline = imageConfig.isStrictNetworkForOffline

        AwImageLogger.d("AwImage.init: memoryCache=${imageConfig.memoryCachePercent}, " +
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

        if (imageConfig.gifEnabled || imageConfig.svgEnabled) {
            builder.components {
                if (imageConfig.gifEnabled) {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                if (imageConfig.svgEnabled) {
                    add(coil.decode.SvgDecoder.Factory())
                }
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
        globalRequestListener = imageConfig.requestListenerForInit

        val imageLoader = builder.build()
        Coil.setImageLoader(imageLoader)
        initialized = true
        AwImageLogger.d("AwImage.init: complete")
        imageLoader
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
            val cache = imageLoader(context).memoryCache
            if (cache == null) {
                AwImageLogger.d("clearMemoryCache: no memory cache")
                return@runCatching false
            }
            cache.clear()
            AwImageLogger.d("clearMemoryCache: success")
            true
        }.onFailure {
            AwImageLogger.e("clearMemoryCache: failed", it)
        }.getOrDefault(false)
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
            val cache = imageLoader(context).diskCache
            if (cache == null) {
                AwImageLogger.d("clearDiskCache: no disk cache")
                return@runCatching false
            }
            cache.clear()
            AwImageLogger.d("clearDiskCache: success")
            true
        }.onFailure {
            AwImageLogger.e("clearDiskCache: failed", it)
        }.getOrDefault(false)
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
            AwImageLogger.e("getMemoryCacheSize: failed", it)
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
            AwImageLogger.e("getDiskCacheSize: failed", it)
        }.getOrDefault(0L)
    }

    /**
     * 在 [android.app.Application.onTrimMemory] / [android.app.Activity.onTrimMemory] 中调用，
     * 在系统回收内存时清理图片**内存**缓存。
     *
     * 在 `level` 为 [ComponentCallbacks2.TRIM_MEMORY_MODERATE] 及以上（后台）或
     * [ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL]（前台极端紧张）时执行 [clearMemoryCache]。
     */
    fun onApplicationTrimMemory(context: Context, level: Int): Boolean {
        val shouldClear = level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE ||
            level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
        return if (shouldClear) clearMemoryCache(context) else false
    }

    /**
     * 检查指定数据源是否已缓存。
     *
     * 使用与 [ImageRequest] 相同的 key 计算方式：先查内存，再查磁盘（命中任一则返回 true）。
     * 若线加载时使用了 [ImageRequest.Builder] 的 [ImageRequest.Builder.override]、
     * [ImageRequest.Builder.transformations] 等，请传入 [requestConfig]，使本方法与真实请求的缓存键一致。
     *
     * @param context      Context
     * @param data         图片数据源（URL / File / @DrawableRes 等）
     * @param requestConfig 可选，与线加载时相同的 Builder 配置（尺寸、变换等）
     * @return `true` 表示已缓存，`false` 表示未缓存或查询失败
     */
    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    @JvmOverloads
    fun isCached(
        context: Context,
        data: Any,
        requestConfig: (ImageRequest.Builder.() -> Unit)? = null
    ): Boolean {
        return runCatching {
            val appContext = context.applicationContext
            val loader = imageLoader(appContext)
            val builder = ImageRequest.Builder(appContext).data(data)
            requestConfig?.invoke(builder)
            val request = builder.build()
            val memKey = request.memoryCacheKey
            if (memKey != null && loader.memoryCache?.get(memKey) != null) {
                return@runCatching true
            }
            val diskKey = request.diskCacheKey
            if (diskKey != null) {
                loader.diskCache?.openSnapshot(diskKey)?.use {
                    return@runCatching true
                }
            }
            false
        }.onFailure {
            AwImageLogger.e("isCached: failed for data=$data", it)
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
        AwImageLogger.d("cancelByTag: cancelled ${disposables.size} requests for tag=$tag")
    }

    /**
     * 取消当前进程内通过 [registerTaggedDisposable] 登记过的**全部**标签请求。
     *
     * 适用于进程级清理或调试；常规场景优先在页面销毁时对具体 tag 调用 [cancelByTag]，
     * 避免误杀其他模块仍需要的加载。
     */
    fun cancelAllTaggedRequests() {
        val keys = taggedDisposables.keys.toList()
        for (k in keys) {
            cancelByTag(k)
        }
        AwImageLogger.d("cancelAllTaggedRequests: cleared ${keys.size} tag(s)")
    }

    internal fun registerTaggedDisposable(tag: Any, disposable: Disposable) {
        val list = taggedDisposables.computeIfAbsent(tag) { CopyOnWriteArrayList() }
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

        private var memoryCacheMaxBytesStore: Long? = null
        internal val memoryCacheMaxBytes: Long? get() = memoryCacheMaxBytesStore

        /** 磁盘缓存最大字节数 */
        var diskCacheSize: Long = 100L * 1024 * 1024
            private set

        private var diskCacheDirStore: File? = null
        internal val diskCacheDir: File? get() = diskCacheDirStore

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

        /**
         * 为 true（默认）时，联网判定需 [android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED]；
         * 为 false 时只要有 INTERNET 即视为在线，减轻强制门户等场景下「误判离线、只走缓存」的情况。
         */
        var isStrictNetworkForOffline: Boolean = true
            private set

        /** 全局占位图资源 ID */
        var placeholderRes: Int = 0
            private set

        private var placeholderDrawableStore: Drawable? = null
        internal val placeholderDrawable: Drawable? get() = placeholderDrawableStore

        /** 全局错误图资源 ID */
        var errorRes: Int = 0
            private set

        private var errorDrawableStore: Drawable? = null
        internal val errorDrawable: Drawable? get() = errorDrawableStore

        /** 全局兜底图资源 ID */
        var fallbackRes: Int = 0
            private set

        private var fallbackDrawableStore: Drawable? = null
        internal val fallbackDrawable: Drawable? get() = fallbackDrawableStore

        private var okHttpClientStore: OkHttpClient? = null
        internal val okHttpClient: OkHttpClient? get() = okHttpClientStore

        private var requestListenerStore: ImageRequest.Listener? = null
        internal val requestListenerForInit: ImageRequest.Listener? get() = requestListenerStore

        /** 按比例设置内存缓存大小（0.05~0.5） */
        fun memoryCacheSize(percent: Double) { memoryCachePercent = percent.coerceIn(0.05, 0.5) }

        /** 按字节数设置内存缓存大小（优先级高于 [memoryCacheSize]） */
        fun memoryCacheMaxSize(bytes: Long) {
            memoryCacheMaxBytesStore = bytes.coerceAtLeast(0)
        }

        /** 设置磁盘缓存最大字节数 */
        fun diskCacheSize(bytes: Long) { diskCacheSize = bytes.coerceAtLeast(0) }

        /** 设置磁盘缓存目录（默认 `{cacheDir}/aw_image_cache`） */
        fun diskCacheDir(directory: File) { diskCacheDirStore = directory }

        /** 设置是否启用全局渐入动画 */
        fun crossfade(enabled: Boolean) { crossfadeEnabled = enabled }

        /** 设置全局渐入动画时长（ms）；为 0 时关闭渐入，与 [AwImageScope.crossfade] 语义一致 */
        fun crossfade(durationMs: Int) {
            crossfadeDuration = durationMs.coerceAtLeast(0)
            crossfadeEnabled = durationMs > 0
        }

        /** 设置是否启用 GIF 解码 */
        fun enableGif(enabled: Boolean) { gifEnabled = enabled }

        /** 设置是否启用 SVG 解码（默认 false） */
        fun enableSvg(enabled: Boolean) { svgEnabled = enabled }

        /** 设置离线/仅缓存策略使用的联网判定是否必须 VALIDATED（默认 true） */
        fun strictNetworkForOffline(enabled: Boolean) { isStrictNetworkForOffline = enabled }

        /**
         * 与每次 [com.answufeng.image.loadImage] 的 DSL 监听器合并（**先**调用本监听器、再 per-request 回调与内部进度清理）。
         */
        fun defaultRequestListener(listener: ImageRequest.Listener) { requestListenerStore = listener }

        /** 设置全局占位图资源 ID */
        fun placeholder(res: Int) { placeholderRes = res }

        /** 设置全局占位图 Drawable（优先级高于资源 ID） */
        fun placeholder(drawable: Drawable) { placeholderDrawableStore = drawable }

        /** 设置全局错误图资源 ID */
        fun error(res: Int) { errorRes = res }

        /** 设置全局错误图 Drawable（优先级高于资源 ID） */
        fun error(drawable: Drawable) { errorDrawableStore = drawable }

        /** 设置全局兜底图资源 ID（data 为 null 时显示） */
        fun fallback(res: Int) { fallbackRes = res }

        /** 设置全局兜底图 Drawable（data 为 null 时显示，优先级高于资源 ID） */
        fun fallback(drawable: Drawable) { fallbackDrawableStore = drawable }

        /** 设置自定义 OkHttpClient（用于自定义超时、拦截器等） */
        fun okHttpClient(client: OkHttpClient) { okHttpClientStore = client }

        /** 设置是否启用调试日志（默认 false） */
        fun enableLogging(enabled: Boolean) { AwImageLogger.enabled = enabled }

        /** 设置 Logcat tag（默认 `aw-image`；空或纯空白则保持默认） */
        fun logTag(name: String) { AwImageLogger.setTag(name) }
    }
}
