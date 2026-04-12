package com.answufeng.image

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
 *     error(R.drawable.error)
 *     circle()
 *     crossfade(300)
 * }
 * ```
 *
 * @param data   图片数据源，为 null 时显示 fallback 或全局错误图
 * @param config 可选的 DSL 配置块
 * @return [Disposable]，始终非 null
 */
fun ImageView.loadImage(
    data: Any?,
    config: (ImageLoadConfig.() -> Unit)? = null
): Disposable {
    if (data == null) {
        AwLogger.d("loadImage: data is null, showing fallback/error")
        val fallback = if (config != null) {
            val c = ImageLoadConfig().apply(config)
            c.fallbackRes.takeIf { it != 0 } ?: AwImage.globalError
        } else {
            AwImage.globalError
        }
        setImageResource(fallback)
        return EMPTY_DISPOSABLE
    }
    val loadConfig = ImageLoadConfig().apply { config?.invoke(this) }

    AwLogger.d("loadImage: data=$data, circle=${loadConfig.isCircle}, " +
            "roundedCorners=${loadConfig.hasRoundedCorners}, cache=${loadConfig.cacheEnabled}")

    return load(data) {
        val ph = loadConfig.placeholderRes.takeIf { it != 0 } ?: AwImage.globalPlaceholder
        if (ph != 0) placeholder(ph)

        val err = loadConfig.errorRes.takeIf { it != 0 } ?: AwImage.globalError
        if (err != 0) error(err)

        if (loadConfig.fallbackRes != 0) fallback(loadConfig.fallbackRes)

        scale(loadConfig.scale)

        if (loadConfig.overrideWidth > 0 && loadConfig.overrideHeight > 0) {
            size(loadConfig.overrideWidth, loadConfig.overrideHeight)
        }

        val transforms = mutableListOf<Transformation>()
        if (loadConfig.isCircle) {
            transforms.add(CircleCropTransformation())
        } else if (loadConfig.hasRoundedCorners) {
            transforms.add(
                RoundedCornersTransformation(
                    loadConfig.cornerTopLeft.takeIf { it >= 0f } ?: loadConfig.cornerRadius,
                    loadConfig.cornerTopRight.takeIf { it >= 0f } ?: loadConfig.cornerRadius,
                    loadConfig.cornerBottomRight.takeIf { it >= 0f } ?: loadConfig.cornerRadius,
                    loadConfig.cornerBottomLeft.takeIf { it >= 0f } ?: loadConfig.cornerRadius,
                )
            )
        }
        loadConfig.customTransformations?.let { transforms.addAll(it) }
        if (transforms.isNotEmpty()) transformations(transforms)

        if (!loadConfig.cacheEnabled) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.DISABLED)
        } else if (loadConfig.cacheOnlyOnOffline && !NetworkMonitor.isConnected(context)) {
            AwLogger.d("loadImage: offline, using cache-only policy")
            networkCachePolicy(CachePolicy.DISABLED)
        }

        when {
            loadConfig.crossfadeDuration > 0 -> crossfade(loadConfig.crossfadeDuration)
            loadConfig.crossfadeEnabled -> crossfade(true)
        }

        val hasStart = loadConfig.onStart != null
        val hasSuccess = loadConfig.onSuccess != null
        val hasError = loadConfig.onError != null
        if (hasStart || hasSuccess || hasError) {
            listener(
                onStart = {
                    AwLogger.d("loadImage: onStart")
                    if (hasStart) loadConfig.onStart!!.invoke()
                },
                onSuccess = { _, result ->
                    AwLogger.d("loadImage: onSuccess")
                    if (hasSuccess) loadConfig.onSuccess!!.invoke(result)
                },
                onError = { _, result ->
                    AwLogger.e("loadImage: onError - ${result.throwable.message}")
                    if (hasError) loadConfig.onError!!.invoke(result)
                },
            )
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

/**
 * [loadImage] 的 DSL 配置类。
 *
 * 通过链式调用配置占位图、缩放、圆角、缓存等属性。
 * 所有属性通过 setter 方法设置，外部不可直接赋值。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     placeholder(R.drawable.loading)
 *     error(R.drawable.error)
 *     fallback(R.drawable.fallback)
 *     circle()
 *     crossfade(300)
 *     noCache()
 *     transform(GrayscaleTransformation())
 *     listener(
 *         onStart = { showProgress() },
 *         onSuccess = { result -> hideProgress() },
 *         onError = { result -> showRetry() }
 *     )
 * }
 * ```
 */
class ImageLoadConfig {
    /** 占位图资源 ID */
    var placeholderRes: Int = 0
        private set
    /** 错误图资源 ID */
    var errorRes: Int = 0
        private set
    /** 兜底图资源 ID（data 为 null 时显示） */
    var fallbackRes: Int = 0
        private set
    /** 图片缩放方式 */
    var scale: Scale = Scale.FIT
        private set
    /** 目标宽度（px），0 表示不限制 */
    var overrideWidth: Int = 0
        private set
    /** 目标高度（px），0 表示不限制 */
    var overrideHeight: Int = 0
        private set
    /** 是否启用圆形裁切（与 [hasRoundedCorners] 互斥，优先级更高） */
    var isCircle: Boolean = false
        private set
    /** 统一圆角半径（px） */
    var cornerRadius: Float = 0f
        private set
    /** 是否设置了圆角 */
    var hasRoundedCorners: Boolean = false
        private set
    /** 左上角圆角半径（px），-1 表示使用 [cornerRadius] */
    var cornerTopLeft: Float = -1f
        private set
    /** 右上角圆角半径（px），-1 表示使用 [cornerRadius] */
    var cornerTopRight: Float = -1f
        private set
    /** 右下角圆角半径（px），-1 表示使用 [cornerRadius] */
    var cornerBottomRight: Float = -1f
        private set
    /** 左下角圆角半径（px），-1 表示使用 [cornerRadius] */
    var cornerBottomLeft: Float = -1f
        private set
    /** 自定义变换列表（累积模式） */
    var customTransformations: List<Transformation>? = null
        private set
    /** 是否启用内存和磁盘缓存 */
    var cacheEnabled: Boolean = true
        private set
    /** 离线时是否仅使用缓存（不发起网络请求） */
    var cacheOnlyOnOffline: Boolean = true
        private set
    /** 是否启用渐入动画 */
    var crossfadeEnabled: Boolean = true
        private set
    /** 渐入动画时长（ms），0 表示使用默认 */
    var crossfadeDuration: Int = 0
        private set

    @Suppress("ktlint")
    internal var onStart: (() -> Unit)? = null
    @Suppress("ktlint")
    internal var onSuccess: ((coil.request.SuccessResult) -> Unit)? = null
    @Suppress("ktlint")
    internal var onError: ((coil.request.ErrorResult) -> Unit)? = null

    /** 设置占位图资源 */
    fun placeholder(res: Int) { placeholderRes = res }
    /** 设置错误图资源 */
    fun error(res: Int) { errorRes = res }
    /** 设置兜底图资源（data 为 null 时显示） */
    fun fallback(res: Int) { fallbackRes = res }
    /** 设置图片缩放方式 */
    fun scale(scale: Scale) { this.scale = scale }
    /** 启用圆形裁切（与 [roundedCorners] 互斥，优先级更高） */
    fun circle() { isCircle = true }
    /** 设置统一圆角半径（px） */
    fun roundedCorners(radius: Float) {
        cornerRadius = radius
        hasRoundedCorners = true
    }
    /** 分别设置四角圆角半径（px） */
    fun roundedCorners(topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float) {
        cornerTopLeft = topLeft; cornerTopRight = topRight
        cornerBottomRight = bottomRight; cornerBottomLeft = bottomLeft
        hasRoundedCorners = true
    }
    /** 指定加载尺寸（px） */
    fun override(width: Int, height: Int) { overrideWidth = width; overrideHeight = height }
    /** 禁用内存和磁盘缓存 */
    fun noCache() { cacheEnabled = false }
    /** 设置离线时是否仅使用缓存（默认 true） */
    fun cacheOnlyOnOffline(enabled: Boolean) { cacheOnlyOnOffline = enabled }
    /**
     * 添加自定义 [Transformation]（累积模式，多次调用不会覆盖）。
     *
     * ```kotlin
     * transform(GrayscaleTransformation(), BlurTransformation())
     * ```
     */
    fun transform(vararg transformations: Transformation) {
        val existing = customTransformations ?: emptyList()
        customTransformations = existing + transformations.toList()
    }
    /** 设置是否启用渐入动画 */
    fun crossfade(enabled: Boolean = true) { crossfadeEnabled = enabled }
    /** 设置渐入动画时长（ms），>0 时自动启用渐入动画 */
    fun crossfade(durationMs: Int) {
        crossfadeDuration = durationMs
        if (durationMs > 0) crossfadeEnabled = true
    }

    /**
     * 设置加载状态监听器。
     *
     * ```kotlin
     * listener(
     *     onStart = { showProgress() },
     *     onSuccess = { result -> hideProgress() },
     *     onError = { result -> showRetry() }
     * )
     * ```
     *
     * @param onStart   加载开始回调
     * @param onSuccess 加载成功回调
     * @param onError   加载失败回调
     */
    fun listener(
        onStart: (() -> Unit)? = null,
        onSuccess: ((coil.request.SuccessResult) -> Unit)? = null,
        onError: ((coil.request.ErrorResult) -> Unit)? = null
    ) {
        this.onStart = onStart
        this.onSuccess = onSuccess
        this.onError = onError
    }
}
