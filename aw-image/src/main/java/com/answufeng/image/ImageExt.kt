package com.answufeng.image

import android.graphics.drawable.Drawable
import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Scale
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import coil.transform.Transformation

private val EMPTY_DISPOSABLE = object : Disposable {
    override val isDisposed get() = true
    override val job = kotlinx.coroutines.CompletableDeferred<coil.request.ImageResult>().apply {
        cancel()
    }
    override fun dispose() {}
}

/**
 * [loadImage] 的 DSL 作用域，直接操作 Coil 的 [ImageRequest.Builder]。
 *
 * 相比旧版 `ImageLoadConfig`，`AwImageScope` 不创建中间配置对象，
 * 而是将 DSL 配置直接映射到 Coil Builder，减少对象分配。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     placeholder(R.drawable.loading)
 *     error(R.drawable.fail)
 *     fallback(ColorDrawable(Color.GRAY))
 *     circle()
 *     crossfade(300)
 *     noCache()
 *     transform(GrayscaleTransformation())
 *     onStart { showProgress() }
 *     onSuccess { result -> hideProgress() }
 *     onError { result -> showRetry() }
 * }
 * ```
 */
class AwImageScope internal constructor(private val builder: ImageRequest.Builder) {

    private val transforms = mutableListOf<Transformation>()
    private var circleEnabled = false
    private var roundedRadius: FloatArray? = null
    private var cacheOnlyOnOffline = true
    private var cacheDisabled = false
    private var crossfadeExplicitlySet = false

    internal var fallbackResId: Int = 0
        private set
    internal var fallbackDrawable: Drawable? = null
        private set

    private var onStartCallback: (() -> Unit)? = null
    private var onSuccessCallback: ((coil.request.SuccessResult) -> Unit)? = null
    private var onErrorCallback: ((coil.request.ErrorResult) -> Unit)? = null

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

    /** 设置兜底图资源 ID（data 为 null 时显示） */
    fun fallback(res: Int) {
        fallbackResId = res
        if (res != 0) builder.fallback(res)
    }

    /** 设置兜底图 Drawable（data 为 null 时显示） */
    fun fallback(drawable: Drawable?) {
        fallbackDrawable = drawable
        if (drawable != null) builder.fallback(drawable)
    }

    /** 设置图片缩放方式 */
    fun scale(scale: Scale) {
        builder.scale(scale)
    }

    /** 启用圆形裁切（与 [roundedCorners] 互斥，优先级更高） */
    fun circle() {
        circleEnabled = true
    }

    /** 设置统一圆角半径（px） */
    fun roundedCorners(radius: Float) {
        roundedRadius = floatArrayOf(radius)
    }

    /** 分别设置四角圆角半径（px） */
    fun roundedCorners(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        roundedRadius = floatArrayOf(topLeft, topRight, bottomRight, bottomLeft)
    }

    /** 指定加载尺寸（px） */
    fun override(width: Int, height: Int) {
        if (width > 0 && height > 0) builder.size(width, height)
    }

    /** 禁用内存和磁盘缓存 */
    fun noCache() {
        cacheDisabled = true
        builder.memoryCachePolicy(CachePolicy.DISABLED)
        builder.diskCachePolicy(CachePolicy.DISABLED)
    }

    /** 设置离线时是否仅使用缓存（默认 true） */
    fun cacheOnlyOnOffline(enabled: Boolean) {
        cacheOnlyOnOffline = enabled
    }

    /**
     * 添加自定义 [Transformation]（累积模式，多次调用不会覆盖）。
     *
     * ```kotlin
     * transform(GrayscaleTransformation(), BlurTransformation())
     * ```
     */
    fun transform(vararg transformations: Transformation) {
        transforms.addAll(transformations)
    }

    /** 设置是否启用渐入动画 */
    fun crossfade(enabled: Boolean = true) {
        crossfadeExplicitlySet = true
        if (enabled) builder.crossfade(true)
    }

    /** 设置渐入动画时长（ms），>0 时自动启用渐入动画 */
    fun crossfade(durationMs: Int) {
        crossfadeExplicitlySet = true
        if (durationMs > 0) builder.crossfade(durationMs)
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
     * 设置加载状态监听器（累积模式，非 null 参数才会覆盖已设置的回调）。
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

        if (!cacheDisabled && cacheOnlyOnOffline && !NetworkMonitor.isConnected(context)) {
            AwLogger.d("loadImage: offline, using cache-only policy")
            builder.networkCachePolicy(CachePolicy.DISABLED)
        }

        val hasStart = onStartCallback != null
        val hasSuccess = onSuccessCallback != null
        val hasError = onErrorCallback != null
        if (hasStart || hasSuccess || hasError) {
            builder.listener(
                onStart = {
                    AwLogger.d("loadImage: onStart")
                    onStartCallback?.invoke()
                },
                onSuccess = { _, result ->
                    AwLogger.d("loadImage: onSuccess")
                    onSuccessCallback?.invoke(result)
                },
                onError = { _, result ->
                    AwLogger.e("loadImage: onError - ${result.throwable.message}")
                    onErrorCallback?.invoke(result)
                },
            )
        }
    }

    internal val isCrossfadeExplicitlySet: Boolean get() = crossfadeExplicitlySet
}

/**
 * 加载图片到 [ImageView]。
 *
 * 支持 URL / File / @DrawableRes / Uri 等数据源。
 * 返回 [Disposable] 用于手动取消加载（Coil 已在 View detach / 新请求时自动取消）。
 *
 * ```kotlin
 * // 最简用法
 * imageView.loadImage("https://example.com/photo.jpg")
 *
 * // 带配置
 * imageView.loadImage(url) {
 *     placeholder(R.drawable.loading)
 *     error(R.drawable.fail)
 *     circle()
 *     crossfade(300)
 * }
 * ```
 *
 * @param data   图片数据源，为 null 时显示 fallback 或全局错误图
 * @param config 可选的 [AwImageScope] DSL 配置块
 * @return [Disposable]，始终非 null
 */
fun ImageView.loadImage(
    data: Any?,
    config: (AwImageScope.() -> Unit)? = null
): Disposable {
    if (data == null) {
        AwLogger.d("loadImage: data is null, showing fallback/error")
        if (config != null) {
            val scope = AwImageScope(ImageRequest.Builder(context).data(0))
            scope.config()
            val fbDrawable = scope.fallbackDrawable
            val fbRes = scope.fallbackResId
            when {
                fbDrawable != null -> setImageDrawable(fbDrawable)
                fbRes != 0 -> setImageResource(fbRes)
                AwImage.globalErrorDrawable != null -> setImageDrawable(AwImage.globalErrorDrawable)
                AwImage.globalError != 0 -> setImageResource(AwImage.globalError)
                else -> setImageResource(0)
            }
        } else {
            when {
                AwImage.globalErrorDrawable != null -> setImageDrawable(AwImage.globalErrorDrawable)
                AwImage.globalError != 0 -> setImageResource(AwImage.globalError)
                else -> setImageResource(0)
            }
        }
        return EMPTY_DISPOSABLE
    }

    AwLogger.d("loadImage: data=$data")

    return load(data) {
        val phDrawable = AwImage.globalPlaceholderDrawable
        val phRes = AwImage.globalPlaceholder
        when {
            phDrawable != null -> placeholder(phDrawable)
            phRes != 0 -> placeholder(phRes)
        }

        val errDrawable = AwImage.globalErrorDrawable
        val errRes = AwImage.globalError
        when {
            errDrawable != null -> error(errDrawable)
            errRes != 0 -> error(errRes)
        }

        if (config != null) {
            val scope = AwImageScope(this)
            scope.config()
            if (!scope.isCrossfadeExplicitlySet) {
                crossfade(true)
            }
            scope.applyTo(context)
        } else {
            crossfade(true)
        }
    }
}

/**
 * 以圆形裁切方式加载图片。
 *
 * 等价于 `loadImage(data) { circle() }`。
 *
 * @param data 图片数据源
 * @return [Disposable] 用于手动取消
 */
fun ImageView.loadCircle(data: Any?): Disposable {
    return loadImage(data) { circle() }
}

/**
 * 以指定圆角加载图片。
 *
 * 等价于 `loadImage(data) { roundedCorners(radiusPx) }`。
 *
 * @param data     图片数据源
 * @param radiusPx 圆角半径（px），必须 >= 0
 * @return [Disposable] 用于手动取消
 * @throws IllegalArgumentException 如果 [radiusPx] < 0
 */
fun ImageView.loadRounded(data: Any?, radiusPx: Float): Disposable {
    require(radiusPx >= 0f) { "radiusPx must be >= 0, got $radiusPx" }
    return loadImage(data) { roundedCorners(radiusPx) }
}

/**
 * 以高斯模糊效果加载图片。
 *
 * 等价于 `loadImage(data) { transform(BlurTransformation(radius, sampling)) }`。
 *
 * @param data     图片数据源
 * @param radius   模糊半径（1~25），默认 15
 * @param sampling 采样因子（≥1），默认 4
 * @return [Disposable] 用于手动取消
 */
fun ImageView.loadBlur(data: Any?, radius: Int = 15, sampling: Int = 4): Disposable {
    return loadImage(data) { transform(BlurTransformation(radius, sampling)) }
}
