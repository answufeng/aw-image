package com.answufeng.image

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.ImageView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil.load
import coil.request.Disposable
import coil.request.ImageRequest

/**
 * 用于生命周期绑定的空 Disposable 占位对象。
 *
 * 当 data 为 null 时直接返回，避免触发 Coil 请求。
 */
private val EMPTY_DISPOSABLE = object : Disposable {
    override val isDisposed get() = true
    override val job = kotlinx.coroutines.CompletableDeferred<coil.request.ImageResult>().apply {
        cancel()
    }
    override fun dispose() {}
}

/**
 * 将全局 crossfade 配置应用到 Coil Builder。
 *
 * 仅在用户未在 DSL 中显式设置 crossfade 时生效。
 */
private fun ImageRequest.Builder.applyGlobalCrossfade() {
    if (AwImage.globalCrossfadeEnabled) {
        crossfade(AwImage.globalCrossfadeDuration)
    }
}

/**
 * 绑定 LifecycleOwner 到 Disposable，在 DESTROYED 事件时自动取消请求。
 *
 * @param disposable 要绑定的 Disposable
 * @param owner LifecycleOwner 实例
 * @param progressToken [AwImageScope] 为 [onProgress] 生成的 token，无则 null
 * @param onProgress 与 token 成对的回调引用，用于 [ProgressInterceptor.unregister]
 */
private fun bindLifecycle(
    disposable: Disposable,
    owner: LifecycleOwner,
    progressToken: String? = null,
    onProgress: ((Long, Long) -> Unit)? = null,
) {
    if (owner.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        if (!disposable.isDisposed) disposable.dispose()
        if (progressToken != null && onProgress != null) {
            ProgressInterceptor.unregister(progressToken, onProgress)
        }
        return
    }
    val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (!disposable.isDisposed) disposable.dispose()
            if (progressToken != null && onProgress != null) {
                ProgressInterceptor.unregister(progressToken, onProgress)
            }
        }
    }
    owner.lifecycle.addObserver(observer)
    disposable.job.invokeOnCompletion {
        owner.lifecycle.removeObserver(observer)
    }
}

/**
 * 当 data 为 null 时，解析并显示 fallback / error 兜底图。
 *
 * 优先级：scope fallback Drawable → scope fallback Res →
 *        global fallback Drawable → global fallback Res →
 *        global error Drawable → global error Res → 清空 ImageView
 */
private fun resolveFallback(imageView: ImageView, scope: AwImageScope?) {
    val fbDrawable = scope?.fallbackDrawable
    val fbRes = scope?.fallbackResId ?: 0
    when {
        fbDrawable != null -> imageView.setImageDrawable(fbDrawable)
        fbRes != 0 -> imageView.setImageResource(fbRes)
        AwImage.globalFallbackDrawable != null -> imageView.setImageDrawable(AwImage.globalFallbackDrawable)
        AwImage.globalFallback != 0 -> imageView.setImageResource(AwImage.globalFallback)
        AwImage.globalErrorDrawable != null -> imageView.setImageDrawable(AwImage.globalErrorDrawable)
        AwImage.globalError != 0 -> imageView.setImageResource(AwImage.globalError)
        else -> imageView.setImageResource(0)
    }
}

/**
 * 加载图片到 [ImageView]。
 *
 * 支持 URL / File / @DrawableRes / Uri 等数据源。
 * 返回 [Disposable] 用于手动取消加载（Coil 已在 View detach / 新请求时自动取消）。
 *
 * 线程约束：此方法应在主线程调用。
 *
 * ```kotlin
 * // 最简用法
 * imageView.loadImage("https://example.com/photo.jpg")
 *
 * // 带常用参数
 * imageView.loadImage(url, placeholderRes = R.drawable.loading, errorRes = R.drawable.fail)
 *
 * // 带完整配置
 * imageView.loadImage(url) {
 *     placeholder(R.drawable.loading)
 *     error(R.drawable.fail)
 *     circle()
 *     crossfade(300)
 * }
 * ```
 *
 * @param data           图片数据源；**为 null** 时不发起 Coil 请求，仅走 [resolveFallback]：
 *                       DSL `fallback` → 全局 fallback → 全局 error → 清空 ImageView。
 *                       此时 DSL 中除 `fallback` 外的大部分请求项（尺寸、变换、网络）无实际作用。
 * @param placeholderRes 占位图资源 ID（0 表示不设置，使用全局配置）
 * @param errorRes       错误图资源 ID（0 表示不设置，使用全局配置）
 * @param config         可选的 [AwImageScope] DSL 配置块
 * @return [Disposable]，始终非 null（data 为 null 时为惰性已释放的占位对象）
 */
fun ImageView.loadImage(
    data: Any?,
    placeholderRes: Int = 0,
    errorRes: Int = 0,
    config: (AwImageScope.() -> Unit)? = null
): Disposable {
    if (data == null) {
        AwImageLogger.d("loadImage: data is null, showing fallback/error")
        val scope = if (config != null) {
            AwImageScope(ImageRequest.Builder(context)).apply(config)
        } else {
            null
        }
        resolveFallback(this, scope)
        return EMPTY_DISPOSABLE
    }

    AwImageLogger.d("loadImage: data=$data")

    var tagValue: Any? = null
    var lifecycleOwner: LifecycleOwner? = null
    var onProgress: ((Long, Long) -> Unit)? = null
    var progressToken: String? = null

    val disposable = load(data) {
        val phDrawable = AwImage.globalPlaceholderDrawable
        val phRes = AwImage.globalPlaceholder
        when {
            placeholderRes != 0 -> placeholder(placeholderRes)
            phDrawable != null -> placeholder(phDrawable)
            phRes != 0 -> placeholder(phRes)
        }

        val errDrawable = AwImage.globalErrorDrawable
        val errRes = AwImage.globalError
        when {
            errorRes != 0 -> error(errorRes)
            errDrawable != null -> error(errDrawable)
            errRes != 0 -> error(errRes)
        }

        if (config != null) {
            val scope = AwImageScope(this, data)
            scope.config()
            if (!scope.isCrossfadeExplicitlySet) {
                applyGlobalCrossfade()
            }
            scope.registerProgressIfNeeded()
            scope.applyTo(context)
            tagValue = scope.tagValue
            lifecycleOwner = scope.lifecycleOwner
            onProgress = scope.onProgressCallback
            progressToken = scope.progressToken
        } else {
            applyGlobalCrossfade()
        }
    }

    lifecycleOwner?.let { owner ->
        bindLifecycle(disposable, owner, progressToken, onProgress)
    }

    if (tagValue != null) {
        AwImage.registerTaggedDisposable(tagValue!!, disposable)
    }

    return disposable
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
 * 以指定圆角加载图片（px 单位）。
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
 * 以指定圆角加载图片（dp 单位）。
 *
 * 自动将 dp 转换为 px。
 *
 * @param data   图片数据源
 * @param radiusDp 圆角半径（dp），必须 >= 0
 * @return [Disposable] 用于手动取消
 * @throws IllegalArgumentException 如果 [radiusDp] < 0
 */
fun ImageView.loadRoundedDp(data: Any?, radiusDp: Float): Disposable {
    require(radiusDp >= 0f) { "radiusDp must be >= 0, got $radiusDp" }
    val px = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, radiusDp, context.resources.displayMetrics
    )
    return loadImage(data) { roundedCorners(px) }
}

/**
 * 以圆形裁切+边框方式加载图片。
 *
 * 适用于头像等圆形带边框场景。
 *
 * @param data         图片数据源
 * @param borderWidth  边框宽度（px），必须 > 0
 * @param borderColor  边框颜色
 * @return [Disposable] 用于手动取消
 * @throws IllegalArgumentException 如果 [borderWidth] <= 0
 */
fun ImageView.loadCircleWithBorder(
    data: Any?,
    borderWidth: Float = 4f,
    borderColor: Int = android.graphics.Color.WHITE
): Disposable {
    require(borderWidth > 0f) { "borderWidth must be > 0, got $borderWidth" }
    return loadImage(data) {
        circle()
        transform(BorderTransformation(borderWidth, borderColor, circle = true))
    }
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
 * 按正方形目标像素解码，适用于头像、列表方图等（减少长边过大导致的解码内存）。
 *
 * 若 [config] 中再次调用 [AwImageScope.override]，以**后者**为准。
 *
 * @param edgePx 边长（px），必须 > 0
 * @throws IllegalArgumentException 当 [edgePx] 非法时
 */
fun ImageView.loadSquare(
    data: Any?,
    edgePx: Int,
    placeholderRes: Int = 0,
    errorRes: Int = 0,
    config: (AwImageScope.() -> Unit)? = null
): Disposable {
    require(edgePx > 0) { "edgePx must be > 0, got $edgePx" }
    return loadImage(data, placeholderRes, errorRes) {
        override(edgePx, edgePx)
        config?.invoke(this)
    }
}

/**
 * 按 [aspectWidth] : [aspectHeight] 比例缩放，使较长边为 [maxEdgePx]（px），用于 16:9 封面等省内存场景。
 *
 * 若 [config] 中再次调用 [AwImageScope.override]，以**后者**为准。
 *
 * @param maxEdgePx 较长边的解码像素，必须 > 0
 * @throws IllegalArgumentException 比例或边长非法时
 */
fun ImageView.loadWithAspectRatio(
    data: Any?,
    aspectWidth: Int,
    aspectHeight: Int,
    maxEdgePx: Int,
    placeholderRes: Int = 0,
    errorRes: Int = 0,
    config: (AwImageScope.() -> Unit)? = null
): Disposable {
    require(aspectWidth > 0 && aspectHeight > 0) {
        "aspectWidth and aspectHeight must be > 0, got ${aspectWidth}x$aspectHeight"
    }
    require(maxEdgePx > 0) { "maxEdgePx must be > 0, got $maxEdgePx" }
    val w: Int
    val h: Int
    if (aspectWidth >= aspectHeight) {
        w = maxEdgePx
        h = (maxEdgePx.toLong() * aspectHeight / aspectWidth).toInt().coerceAtLeast(1)
    } else {
        h = maxEdgePx
        w = (maxEdgePx.toLong() * aspectWidth / aspectHeight).toInt().coerceAtLeast(1)
    }
    return loadImage(data, placeholderRes, errorRes) {
        override(w, h)
        config?.invoke(this)
    }
}
