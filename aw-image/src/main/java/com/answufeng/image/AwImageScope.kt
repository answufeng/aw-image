package com.answufeng.image

import android.graphics.drawable.Drawable
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import coil.transform.Transformation
import okhttp3.Headers
import java.util.UUID

/**
 * [loadImage] 的 DSL 作用域，直接操作 Coil 的 [ImageRequest.Builder]。
 *
 * 相比旧版 `ImageLoadConfig`，`AwImageScope` 不创建中间配置对象，
 * 而是将 DSL 配置直接映射到 Coil Builder，减少对象分配。
 *
 * ## 线程
 * 应在 **主线程** 上调用 [loadImage][com.answufeng.image.loadImage] 及其 DSL
 *（与 `ImageView`、Coil 常规用法一致）。预加载、磁盘缓存等后台工作由 Coil 调度，无需在此处理。
 *
 * ## 与 Coil 的边界
 * 需要本库未封装的参数时，使用 [addHeader] / [setHeader]、[memoryCachePolicy]、[networkCachePolicy] 等，
 * 或使用 [raw] 直接配置 [ImageRequest.Builder]。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     placeholder(R.drawable.loading)
 *     error(R.drawable.fail)
 *     fallback(ColorDrawable(Color.GRAY))
 *     circle()
 *     crossfade(300)
 *     disableCache()
 *     transform(GrayscaleTransformation())
 *     listener(
 *         onStart = { showProgress() },
 *         onSuccess = { result -> hideProgress() },
 *         onError = { result -> showRetry() }
 *     )
 * }
 * ```
 */
@AwImageDsl
class AwImageScope internal constructor(
    private val builder: ImageRequest.Builder,
    /**
     * 与本次请求的 [ImageRequest.data] 一致（Coil 新版 [ImageRequest.Builder] 的 `data` 字段不可读）。
     */
    internal val requestData: Any? = null,
) {

    private val transforms = mutableListOf<Transformation>()
    private var circleEnabled = false
    private var roundedRadius: FloatArray? = null
    private var offlineCacheEnabled = true
    private var cacheDisabled = false
    private var memoryCacheOnlyEnabled = false
    private var crossfadeExplicitlySet = false

    /** [raw] 块在 [applyTo] 尾部依次执行，用于补全任意 Builder 配置。 */
    private val rawBlocks = mutableListOf<ImageRequest.Builder.() -> Unit>()

    internal var tagValue: Any? = null
        private set
    internal var lifecycleOwner: androidx.lifecycle.LifecycleOwner? = null
        private set

    internal var fallbackResId: Int = 0
        private set
    internal var fallbackDrawable: Drawable? = null
        private set

    private var onStartCallback: (() -> Unit)? = null
    private var onSuccessCallback: ((coil.request.SuccessResult) -> Unit)? = null
    private var onErrorCallback: ((coil.request.ErrorResult) -> Unit)? = null

    internal fun setRetryOnError(retryOnError: ((coil.request.ErrorResult) -> Unit)?) {
        if (retryOnError == null) {
            onErrorCallback = null
            return
        }
        val previous = onErrorCallback
        onErrorCallback = { result ->
            previous?.invoke(result)
            retryOnError(result)
        }
    }
    internal var onProgressCallback: ((Long, Long) -> Unit)? = null
        private set

    internal var retryCount: Int = 0
        private set

    internal var retryOnNetworkReconnect: Boolean = false
        private set

    /**
     * 与 [onProgress] 配套的唯一 token，写入 OkHttp 请求头并在 [ProgressInterceptor] 中剥离。
     * 用于区分同一 URL 的并发下载进度。
     */
    internal var progressToken: String? = null
        private set

    /** 设置占位图资源 ID */
    fun placeholder(res: Int) {
        if (res != 0) builder.placeholder(res)
    }

    /** 设置占位图 Drawable */
    fun placeholder(drawable: Drawable?) {
        if (drawable != null) builder.placeholder(drawable)
    }

    /** 设置错误图资源 ID */
    fun error(res: Int) {
        if (res != 0) builder.error(res)
    }

    /** 设置错误图 Drawable */
    fun error(drawable: Drawable?) {
        if (drawable != null) builder.error(drawable)
    }

    /**
     * 设置兜底图资源 ID（data 为 null 时显示）。
     *
     * 若未设置 fallback，会依次回退到全局 fallback → 全局 error。
     */
    fun fallback(res: Int) {
        fallbackResId = res
        if (res != 0) builder.fallback(res)
    }

    /**
     * 设置兜底图 Drawable（data 为 null 时显示）。
     *
     * 优先级高于 [fallback] 资源 ID 设置。
     */
    fun fallback(drawable: Drawable?) {
        fallbackDrawable = drawable
        if (drawable != null) builder.fallback(drawable)
    }

    /** 设置图片缩放方式 */
    fun scale(scale: Scale) {
        builder.scale(scale)
    }

    /**
     * 启用圆形裁切。
     *
     * 与 [roundedCorners] 互斥，优先级更高。
     */
    fun circle() {
        circleEnabled = true
    }

    /**
     * 设置统一圆角半径（px 单位）。
     *
     * @param radius 圆角半径，必须 >= 0
     */
    fun roundedCorners(radius: Float) {
        roundedRadius = floatArrayOf(radius.coerceAtLeast(0f))
    }

    /**
     * 分别设置四角圆角半径（px 单位）。
     *
     * @param topLeft 左上角半径
     * @param topRight 右上角半径
     * @param bottomRight 右下角半径
     * @param bottomLeft 左下角半径
     */
    fun roundedCorners(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        roundedRadius = floatArrayOf(
            topLeft.coerceAtLeast(0f),
            topRight.coerceAtLeast(0f),
            bottomRight.coerceAtLeast(0f),
            bottomLeft.coerceAtLeast(0f),
        )
    }

    /**
     * 指定加载尺寸（px 单位）。
     *
     * @param width 宽度，必须 > 0
     * @param height 高度，必须 > 0
     * @throws IllegalArgumentException 当 [width] 或 [height] 非正时
     */
    fun override(width: Int, height: Int) {
        require(width > 0 && height > 0) {
            "override width and height must be > 0, got ${width}x$height"
        }
        builder.size(width, height)
    }

    /**
     * 自定义内存缓存键（与 [com.answufeng.image.AwImage.isCached]、预加载的 `size`/变换配置需一致才能命中）。
     * 映射 [ImageRequest.Builder.memoryCacheKey]。
     */
    fun memoryCacheKey(key: String?) {
        builder.memoryCacheKey(key)
    }

    /**
     * 自定义磁盘缓存键。映射 [ImageRequest.Builder.diskCacheKey]。
     */
    fun diskCacheKey(key: String?) {
        builder.diskCacheKey(key)
    }

    /**
     * 禁用内存和磁盘缓存。
     *
     * 适用于不需要缓存的场景，如实时验证码图片。
     */
    fun disableCache() {
        cacheDisabled = true
        builder.memoryCachePolicy(CachePolicy.DISABLED)
        builder.diskCachePolicy(CachePolicy.DISABLED)
    }

    /** @suppress 使用 [disableCache] 替代 */
    @Deprecated("Use disableCache instead", ReplaceWith("disableCache()"))
    fun noCache() = disableCache()

    /**
     * 设置离线时是否仅使用缓存（默认 true）。
     *
     * 启用后，当设备无网络连接时，自动禁用网络请求，仅从内存/磁盘缓存读取图片。
     *
     * @param enabled 是否启用离线缓存，默认 true
     */
    fun offlineCacheEnabled(enabled: Boolean) {
        offlineCacheEnabled = enabled
    }

    /** @suppress 使用 [offlineCacheEnabled] 替代 */
    @Deprecated("Use offlineCacheEnabled instead", ReplaceWith("offlineCacheEnabled(enabled)"))
    fun cacheOnlyOnOffline(enabled: Boolean) {
        offlineCacheEnabled = enabled
    }

    /**
     * 仅从内存缓存读取（跳过磁盘和网络）。
     *
     * 适用于极高频刷新场景，如 RecyclerView 快速滑动时避免磁盘 IO。
     */
    fun memoryCacheOnly() {
        memoryCacheOnlyEnabled = true
        builder.diskCachePolicy(CachePolicy.DISABLED)
        builder.networkCachePolicy(CachePolicy.DISABLED)
    }

    /**
     * 细粒度控制**内存**缓存读写（与 [disableCache]、[memoryCacheOnly] 二选一更稳妥，不要混用矛盾策略）。
     *
     * 映射 [ImageRequest.Builder.memoryCachePolicy]。
     */
    fun memoryCachePolicy(policy: CachePolicy) {
        builder.memoryCachePolicy(policy)
    }

    /**
     * 细粒度控制**磁盘**缓存读写。
     */
    fun diskCachePolicy(policy: CachePolicy) {
        builder.diskCachePolicy(policy)
    }

    /**
     * 细粒度控制**网络**使用（如仅禁用网络、仍写磁盘，视 Coil 行为而定）。
     */
    fun networkCachePolicy(policy: CachePolicy) {
        builder.networkCachePolicy(policy)
    }

    /**
     * 为本次网络请求增加 HTTP 头（CDN 鉴权、User-Agent 等）。映射 [ImageRequest.Builder.addHeader]。
     */
    fun addHeader(name: String, value: String) {
        builder.addHeader(name, value)
    }

    /**
     * 设置/覆盖单条请求头。映射 [ImageRequest.Builder.setHeader]。
     */
    fun setHeader(name: String, value: String) {
        builder.setHeader(name, value)
    }

    /**
     * 移除指定名称的请求头。映射 [ImageRequest.Builder.removeHeader]。
     */
    fun removeHeader(name: String) {
        builder.removeHeader(name)
    }

    /**
     * 以 [Headers] 整体替换本请求将使用的头集合。映射 [ImageRequest.Builder.headers]。
     */
    fun headers(headers: Headers) {
        builder.headers(headers)
    }

    /**
     * 多阶段/占位图优化：用内存中已有位图的缓存键作为「占位图」源（如先显示缩略、再全图）。
     * 映射 [ImageRequest.Builder.placeholderMemoryCacheKey]（字符串形式）。
     */
    fun placeholderMemoryCacheKey(key: String?) {
        builder.placeholderMemoryCacheKey(key)
    }

    /**
     * 高级：在 [applyTo] **即将**包装监听器前，对 [ImageRequest.Builder] 做最后一轮配置。
     *
     * 多次调用会**按顺序**全部执行。适合 [ImageRequest] 中本库未单独封装的项。
     *
     * **重要**：[circle]、[transform] 等由 [applyTo] 在 `raw` **之前**调用
     * [ImageRequest.Builder.transformations]，因此 **不要** 在 `raw` 中再设 `transformations`，
     * 否则会被库侧变换覆盖；请求头、单独 `size`、额外 `tag` 等可安全放在 `raw` 中。
     * **不要** 移除或覆盖 [ProgressInterceptor.PROGRESS_TOKEN_HEADER]（[onProgress] 依赖）。
     *
     * Coil [ImageRequest.Builder] 全量 API 见官方文档：
     * https://coil-kt.github.io/coil/api/coil-request/-image-request/-builder/
     */
    fun raw(block: ImageRequest.Builder.() -> Unit) {
        rawBlocks.add(block)
    }

    /**
     * 添加自定义 [Transformation]（累积模式，多次调用不会覆盖）。
     *
     * ```kotlin
     * transform(GrayscaleTransformation(), BlurTransformation())
     * ```
     *
     * @param transformations 一个或多个变换实例
     */
    fun transform(vararg transformations: Transformation) {
        transforms.addAll(transformations)
    }

    /**
     * 设置是否启用渐入动画。
     *
     * @param enabled 是否启用，默认 true
     */
    fun crossfade(enabled: Boolean = true) {
        crossfadeExplicitlySet = true
        builder.crossfade(enabled)
    }

    /**
     * 设置渐入动画时长（ms）。
     *
     * @param durationMs 动画时长，> 0 时自动启用渐入动画
     */
    fun crossfade(durationMs: Int) {
        crossfadeExplicitlySet = true
        if (durationMs > 0) {
            builder.crossfade(durationMs)
        } else {
            builder.crossfade(false)
        }
    }

    /**
     * 设置请求标签，用于批量取消。
     *
     * ```kotlin
     * imageView.loadImage(url) { tag("feed_list") }
     * // 退出页面时批量取消
     * AwImage.cancelByTag("feed_list")
     * ```
     *
     * @param key 请求标签，任意非 null 对象
     */
    fun tag(key: Any) {
        tagValue = key
        builder.tag(key)
    }

    /**
     * 绑定 [androidx.lifecycle.LifecycleOwner]，在 Lifecycle DESTROYED 时自动取消请求。
     *
     * ```kotlin
     * imageView.loadImage(url) {
     *     lifecycle(this@MyActivity)
     * }
     * ```
     *
     * @param owner LifecycleOwner 实例（Activity / Fragment 等）
     */
    fun lifecycle(owner: androidx.lifecycle.LifecycleOwner) {
        lifecycleOwner = owner
    }

    /**
     * 设置加载状态监听器（覆盖模式，非 null 参数才会覆盖已设置的回调）。
     *
     * ```kotlin
     * listener(
     *     onStart = { showProgress() },
     *     onSuccess = { result -> hideProgress() },
     *     onError = { result -> showRetry() }
     * )
     * ```
     */
    fun listener(
        onStart: (() -> Unit)? = null,
        onSuccess: ((coil.request.SuccessResult) -> Unit)? = null,
        onError: ((coil.request.ErrorResult) -> Unit)? = null
    ) {
        onStart?.let { onStartCallback = it }
        onSuccess?.let { onSuccessCallback = it }
        onError?.let { onErrorCallback = it }
    }

    /** 设置加载开始回调 */
    fun onStart(action: () -> Unit) {
        onStartCallback = action
    }

    /** 设置加载成功回调 */
    fun onSuccess(action: (coil.request.SuccessResult) -> Unit) {
        onSuccessCallback = action
    }

    /** 设置加载失败回调 */
    fun onError(action: (coil.request.ErrorResult) -> Unit) {
        onErrorCallback = action
    }

    /**
     * 设置下载进度回调。
     *
     * 回调可能在 **非主线程** 执行（OkHttp 读流线程）；更新 UI 时请 `view.post { }` 或切主线程。
     * 仅 **http(s) 字符串** data 且经本库 [ProgressInterceptor] 时有效。
     *
     * @param action 回调参数为 (currentBytes, totalBytes)，totalBytes 为 -1 时表示未知
     */
    fun onProgress(action: (currentBytes: Long, totalBytes: Long) -> Unit) {
        onProgressCallback = action
    }

    fun retry(count: Int) {
        require(count >= 0) { "retryCount must be >= 0, got $count" }
        retryCount = count
    }

    fun retryOnNetworkReconnect(enabled: Boolean = true) {
        retryOnNetworkReconnect = enabled
    }

    internal fun applyTo(context: android.content.Context) {
        if (circleEnabled) {
            transforms.add(CircleCropTransformation())
        } else {
            roundedRadius?.let { r ->
                if (r.size == 1) {
                    transforms.add(RoundedCornersTransformation(r[0]))
                } else {
                    transforms.add(RoundedCornersTransformation(r[0], r[1], r[2], r[3]))
                }
            }
        }
        if (transforms.isNotEmpty()) builder.transformations(transforms)

        if (!cacheDisabled && !memoryCacheOnlyEnabled && offlineCacheEnabled && !ImageNetworkMonitor.isConnected(context)) {
            AwImageLogger.d("loadImage: offline, using cache-only policy")
            builder.networkCachePolicy(CachePolicy.DISABLED)
        }

        for (block in rawBlocks) {
            block(builder)
        }

        val hasStart = onStartCallback != null
        val hasSuccess = onSuccessCallback != null
        val hasError = onErrorCallback != null
        val hasProgress = onProgressCallback != null
        val globalListener = AwImage.globalRequestListener
        if (hasStart || hasSuccess || hasError || hasProgress || globalListener != null) {
            val tokenSnapshot = progressToken
            val progressCb = onProgressCallback
            builder.listener(
                onStart = { request ->
                    globalListener?.onStart(request)
                    AwImageLogger.d("loadImage: onStart")
                    onStartCallback?.invoke()
                },
                onCancel = { request ->
                    globalListener?.onCancel(request)
                    if (tokenSnapshot != null && progressCb != null) {
                        ProgressInterceptor.unregister(tokenSnapshot, progressCb)
                    }
                },
                onError = { request, result ->
                    globalListener?.onError(request, result)
                    AwImageLogger.e("loadImage: onError - ${result.throwable.message}")
                    if (tokenSnapshot != null && progressCb != null) {
                        ProgressInterceptor.unregister(tokenSnapshot, progressCb)
                    }
                    onErrorCallback?.invoke(result)
                },
                onSuccess = { request, result ->
                    globalListener?.onSuccess(request, result)
                    AwImageLogger.d("loadImage: onSuccess")
                    if (tokenSnapshot != null && progressCb != null) {
                        ProgressInterceptor.unregister(tokenSnapshot, progressCb)
                    }
                    onSuccessCallback?.invoke(result)
                },
            )
        }
    }

    internal fun registerProgressIfNeeded() {
        val callback = onProgressCallback ?: return
        val data = requestData ?: return
        if (data !is String) return
        val token = UUID.randomUUID().toString()
        progressToken = token
        ProgressInterceptor.register(token, callback)
        builder.addHeader(ProgressInterceptor.PROGRESS_TOKEN_HEADER, token)
    }

    internal val isCrossfadeExplicitlySet: Boolean get() = crossfadeExplicitlySet
}
