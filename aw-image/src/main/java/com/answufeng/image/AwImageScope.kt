package com.answufeng.image

import android.graphics.drawable.Drawable
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import coil.transform.Transformation

/**
 * [loadImage] 的 DSL 作用域，直接操作 Coil 的 [ImageRequest.Builder]。
 *
 * 相比旧版 `ImageLoadConfig`，`AwImageScope` 不创建中间配置对象，
 * 而是将 DSL 配置直接映射到 Coil Builder，减少对象分配。
 *
 * 线程约束：此类的实例应在主线程创建和使用。涉及的 Coil Builder
 * 操作最终会在 `applyTo` 中统一提交。
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
class AwImageScope internal constructor(private val builder: ImageRequest.Builder) {

    private val transforms = mutableListOf<Transformation>()
    private var circleEnabled = false
    private var roundedRadius: FloatArray? = null
    private var offlineCacheEnabled = true
    private var cacheDisabled = false
    private var memoryCacheOnlyEnabled = false
    private var crossfadeExplicitlySet = false
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
    internal var onProgressCallback: ((Long, Long) -> Unit)? = null
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
        roundedRadius = floatArrayOf(radius)
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
        roundedRadius = floatArrayOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    /**
     * 指定加载尺寸（px 单位）。
     *
     * @param width 宽度，必须 > 0
     * @param height 高度，必须 > 0
     */
    fun override(width: Int, height: Int) {
        if (width > 0 && height > 0) builder.size(width, height)
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
        if (enabled) builder.crossfade(true)
    }

    /**
     * 设置渐入动画时长（ms）。
     *
     * @param durationMs 动画时长，> 0 时自动启用渐入动画
     */
    fun crossfade(durationMs: Int) {
        crossfadeExplicitlySet = true
        if (durationMs > 0) builder.crossfade(durationMs)
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
     * @param action 回调参数为 (currentBytes, totalBytes)，totalBytes 为 -1 时表示未知
     */
    fun onProgress(action: (currentBytes: Long, totalBytes: Long) -> Unit) {
        onProgressCallback = action
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

        val hasStart = onStartCallback != null
        val hasSuccess = onSuccessCallback != null
        val hasError = onErrorCallback != null
        val hasProgress = onProgressCallback != null
        if (hasStart || hasSuccess || hasError || hasProgress) {
            builder.listener(
                onStart = {
                    AwImageLogger.d("loadImage: onStart")
                    onStartCallback?.invoke()
                },
                onSuccess = { _, result ->
                    AwImageLogger.d("loadImage: onSuccess")
                    val url = result.request.data?.toString()
                    if (url != null) ProgressInterceptor.unregister(url)
                    onSuccessCallback?.invoke(result)
                },
                onError = { _, result ->
                    AwImageLogger.e("loadImage: onError - ${result.throwable.message}")
                    val url = result.request.data?.toString()
                    if (url != null) ProgressInterceptor.unregister(url)
                    onErrorCallback?.invoke(result)
                },
            )
        }
    }

    internal fun registerProgressIfNeeded() {
        val callback = onProgressCallback ?: return
        val data = builder.data ?: return
        if (data is String) {
            ProgressInterceptor.register(data, callback)
        }
    }

    internal val isCrossfadeExplicitlySet: Boolean get() = crossfadeExplicitlySet
}
