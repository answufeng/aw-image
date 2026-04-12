package com.answufeng.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.min

/**
 * 灰度变换——将图片转为黑白灰度效果。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(GrayscaleTransformation())
 * }
 * ```
 */
class GrayscaleTransformation : Transformation {
    override val cacheKey = "aw_grayscale"
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        val config = input.config ?: Bitmap.Config.ARGB_8888
        val output = Bitmap.createBitmap(input.width, input.height, config)
        Canvas(output).drawBitmap(input, 0f, 0f, paint)
        return output
    }
}

/**
 * 颜色滤镜变换——在图片上叠加指定颜色。
 *
 * 使用 [PorterDuff.Mode.SRC_ATOP] 混合模式，保留原图亮度信息。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(ColorFilterTransformation(0x80FF0000.toInt()))  // 半透明红色滤镜
 * }
 * ```
 *
 * @param color 滤镜颜色（支持 alpha 通道）
 */
class ColorFilterTransformation(private val color: Int) : Transformation {
    override val cacheKey = "aw_color_filter_${Integer.toHexString(color)}"
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val config = input.config ?: Bitmap.Config.ARGB_8888
        val output = Bitmap.createBitmap(input.width, input.height, config)
        val canvas = Canvas(output)
        canvas.drawBitmap(input, 0f, 0f, null)
        canvas.drawColor(color, PorterDuff.Mode.SRC_ATOP)
        return output
    }
}

/**
 * 边框变换——为图片绘制边框，支持矩形和圆形。
 *
 * 当 [circle] 为 true 时绘制圆形边框，否则绘制矩形边框。
 * 配合 [coil.transform.CircleCropTransformation] 使用时，应将 [circle] 设为 true。
 *
 * ```kotlin
 * // 矩形边框
 * imageView.loadImage(url) {
 *     transform(BorderTransformation(4f, Color.RED))
 * }
 *
 * // 圆形边框（配合圆形裁切）
 * imageView.loadImage(url) {
 *     circle()
 *     transform(BorderTransformation(4f, Color.RED, circle = true))
 * }
 * ```
 *
 * @param borderWidth 边框宽度（px），必须 > 0
 * @param borderColor 边框颜色
 * @param circle      是否绘制圆形边框，默认 false
 * @throws IllegalArgumentException 如果 [borderWidth] <= 0
 */
class BorderTransformation(
    private val borderWidth: Float,
    private val borderColor: Int,
    private val circle: Boolean = false
) : Transformation {

    init {
        require(borderWidth > 0f) { "borderWidth must be > 0, got $borderWidth" }
    }

    override val cacheKey = "aw_border_${borderWidth}_${Integer.toHexString(borderColor)}_$circle"
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val config = input.config ?: Bitmap.Config.ARGB_8888
        val output = Bitmap.createBitmap(input.width, input.height, config)
        val canvas = Canvas(output)
        canvas.drawBitmap(input, 0f, 0f, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
        if (circle) {
            val cx = input.width / 2f
            val cy = input.height / 2f
            val maxRadius = minOf(input.width, input.height) / 2f
            val radius = (maxRadius - borderWidth).coerceAtLeast(0f)
            if (radius > 0f) {
                canvas.drawCircle(cx, cy, radius, paint)
            }
        } else {
            val half = borderWidth / 2f
            val right = (input.width - half).coerceAtLeast(half)
            val bottom = (input.height - half).coerceAtLeast(half)
            canvas.drawRect(half, half, right, bottom, paint)
        }
        return output
    }
}

/**
 * 模糊变换——对图片施加高斯模糊效果。
 *
 * API 31+ 使用 `RenderEffect` 硬件加速模糊（通过反射调用，兼容所有 compileSdk），
 * 低版本回退到 StackBlur 算法：
 * 1. 先将图片缩小到 [sampling] 比例（默认 1/4），大幅降低像素处理量
 * 2. 对缩小后的 Bitmap 执行 StackBlur 算法（O(w*h)，与 radius 无关）
 * 3. 再放大回原始尺寸，缩放本身提供额外平滑效果
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(BlurTransformation())           // 默认 radius=15, sampling=4
 *     transform(BlurTransformation(25, 2))      // 更强模糊，较少采样
 * }
 * ```
 *
 * @param radius   模糊半径（1~25），值越大越模糊
 * @param sampling 采样因子（≥1），值越大处理越快但细节越少
 * @throws IllegalArgumentException 如果 [radius] 不在 1~25 范围内，或 [sampling] < 1
 */
class BlurTransformation(
    private val radius: Int = 15,
    private val sampling: Int = 4
) : Transformation {

    init {
        require(radius in 1..25) { "blur radius must be in 1..25, got $radius" }
        require(sampling >= 1) { "sampling must be >= 1, got $sampling" }
    }

    override val cacheKey = "aw_blur_${radius}_$sampling"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (input.width <= 0 || input.height <= 0) return input

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val result = applyRenderEffectBlur(input)
            if (result != null) return result
        }

        return applyStackBlur(input)
    }

    private fun applyRenderEffectBlur(input: Bitmap): Bitmap? {
        return runCatching {
            val renderEffectClass = Class.forName("android.graphics.RenderEffect")
            val tileModeClass = Class.forName("android.graphics.Shader\$TileMode")
            val clamp = tileModeClass.enumConstants.first { (it as Enum<*>).name == "CLAMP" }

            val createBlurMethod = renderEffectClass.getMethod(
                "createBlurEffect",
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                tileModeClass
            )
            val blurEffect = createBlurMethod.invoke(null, radius.toFloat(), radius.toFloat(), clamp)

            val output = Bitmap.createBitmap(input.width, input.height,
                input.config ?: Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val setRenderEffectMethod = Paint::class.java.getMethod(
                "setRenderEffect", renderEffectClass
            )
            setRenderEffectMethod.invoke(paint, blurEffect)
            canvas.drawBitmap(input, 0f, 0f, paint)
            output
        }.onFailure {
            AwLogger.e("applyRenderEffectBlur: failed, falling back to StackBlur", it)
        }.getOrNull()
    }

    private fun applyStackBlur(input: Bitmap): Bitmap {
        val scaledWidth = (input.width / sampling).coerceAtLeast(1)
        val scaledHeight = (input.height / sampling).coerceAtLeast(1)

        val scaledBitmap = Bitmap.createScaledBitmap(input, scaledWidth, scaledHeight, true)

        val blurred = StackBlur.blur(scaledBitmap, radius)
        scaledBitmap.recycle()

        if (blurred.width == input.width && blurred.height == input.height) return blurred
        val output = Bitmap.createScaledBitmap(blurred, input.width, input.height, true)
        if (output !== blurred) blurred.recycle()
        return output
    }
}

/**
 * StackBlur 算法实现——高性能 O(w*h) 模糊，与 radius 无关。
 *
 * 基于 Mario Klingemann 的 StackBlur 算法，使用滑动窗口累加器，
 * 水平+垂直两趟扫描完成模糊，对 RGBA 四通道独立处理。
 *
 * @see <a href="http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html">StackBlur</a>
 */
internal object StackBlur {

    fun blur(bitmap: Bitmap, radius: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return bitmap

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val div = 2 * radius + 1
        val divSum = (div + 1) shr 1
        val divSumSq = divSum * divSum

        val r = IntArray(w * h)
        val g = IntArray(w * h)
        val b = IntArray(w * h)
        val a = IntArray(w * h)
        val vMin = IntArray(maxOf(w, h))
        val stack = Array(div) { IntArray(4) }

        for (y in 0 until h) {
            horizontalPass(pixels, r, g, b, a, stack, vMin, w, h, y, radius, div, divSumSq)
        }

        for (x in 0 until w) {
            verticalPass(pixels, r, g, b, a, stack, vMin, w, h, x, radius, div, divSumSq)
        }

        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(w, h, config).also {
            it.setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    private fun horizontalPass(
        pixels: IntArray, r: IntArray, g: IntArray, b: IntArray, a: IntArray,
        stack: Array<IntArray>, vMin: IntArray, w: Int, h: Int, y: Int,
        radius: Int, div: Int, divSumSq: Int,
    ) {
        val sumR = IntArray(1); val sumG = IntArray(1); val sumB = IntArray(1); val sumA = IntArray(1)
        val inSumR = IntArray(1); val inSumG = IntArray(1); val inSumB = IntArray(1); val inSumA = IntArray(1)
        val outSumR = IntArray(1); val outSumG = IntArray(1); val outSumB = IntArray(1); val outSumA = IntArray(1)

        for (i in -radius..radius) {
            val p = pixels[y * w + min(w - 1, maxOf(i, 0))]
            val sir = stack[i + radius]
            sir[0] = (p shr 16) and 0xff
            sir[1] = (p shr 8) and 0xff
            sir[2] = p and 0xff
            sir[3] = (p ushr 24) and 0xff
            val weight = radius + 1 - kotlin.math.abs(i)
            sumR[0] += sir[0] * weight; sumG[0] += sir[1] * weight
            sumB[0] += sir[2] * weight; sumA[0] += sir[3] * weight
            if (i > 0) {
                inSumR[0] += sir[0]; inSumG[0] += sir[1]
                inSumB[0] += sir[2]; inSumA[0] += sir[3]
            } else {
                outSumR[0] += sir[0]; outSumG[0] += sir[1]
                outSumB[0] += sir[2]; outSumA[0] += sir[3]
            }
        }

        var sp = radius
        for (x in 0 until w) {
            val idx = y * w + x
            r[idx] = (sumR[0] / divSumSq).coerceIn(0, 255)
            g[idx] = (sumG[0] / divSumSq).coerceIn(0, 255)
            b[idx] = (sumB[0] / divSumSq).coerceIn(0, 255)
            a[idx] = (sumA[0] / divSumSq).coerceIn(0, 255)

            sumR[0] -= outSumR[0]; sumG[0] -= outSumG[0]
            sumB[0] -= outSumB[0]; sumA[0] -= outSumA[0]

            val si = (sp - radius + div) % div
            val sir = stack[si]
            outSumR[0] -= sir[0]; outSumG[0] -= sir[1]
            outSumB[0] -= sir[2]; outSumA[0] -= sir[3]

            if (y == 0) vMin[x] = min(x + radius + 1, w - 1)
            val p = pixels[y * w + vMin[x]]
            sir[0] = (p shr 16) and 0xff; sir[1] = (p shr 8) and 0xff
            sir[2] = p and 0xff; sir[3] = (p ushr 24) and 0xff
            inSumR[0] += sir[0]; inSumG[0] += sir[1]
            inSumB[0] += sir[2]; inSumA[0] += sir[3]
            sumR[0] += inSumR[0]; sumG[0] += inSumG[0]
            sumB[0] += inSumB[0]; sumA[0] += inSumA[0]

            sp = (sp + 1) % div
            val sir2 = stack[sp]
            outSumR[0] += sir2[0]; outSumG[0] += sir2[1]
            outSumB[0] += sir2[2]; outSumA[0] += sir2[3]
            inSumR[0] -= sir2[0]; inSumG[0] -= sir2[1]
            inSumB[0] -= sir2[2]; inSumA[0] -= sir2[3]
        }
    }

    private fun verticalPass(
        pixels: IntArray, r: IntArray, g: IntArray, b: IntArray, a: IntArray,
        stack: Array<IntArray>, vMin: IntArray, w: Int, h: Int, x: Int,
        radius: Int, div: Int, divSumSq: Int,
    ) {
        val sumR = IntArray(1); val sumG = IntArray(1); val sumB = IntArray(1); val sumA = IntArray(1)
        val inSumR = IntArray(1); val inSumG = IntArray(1); val inSumB = IntArray(1); val inSumA = IntArray(1)
        val outSumR = IntArray(1); val outSumG = IntArray(1); val outSumB = IntArray(1); val outSumA = IntArray(1)

        for (i in -radius..radius) {
            val yp = min(h - 1, maxOf(i, 0))
            val idx = yp * w + x
            val sir = stack[i + radius]
            sir[0] = r[idx]; sir[1] = g[idx]; sir[2] = b[idx]; sir[3] = a[idx]
            val weight = radius + 1 - kotlin.math.abs(i)
            sumR[0] += sir[0] * weight; sumG[0] += sir[1] * weight
            sumB[0] += sir[2] * weight; sumA[0] += sir[3] * weight
            if (i > 0) {
                inSumR[0] += sir[0]; inSumG[0] += sir[1]
                inSumB[0] += sir[2]; inSumA[0] += sir[3]
            } else {
                outSumR[0] += sir[0]; outSumG[0] += sir[1]
                outSumB[0] += sir[2]; outSumA[0] += sir[3]
            }
        }

        var sp = radius
        for (y in 0 until h) {
            val idx = y * w + x
            pixels[idx] = ((sumA[0] / divSumSq).coerceIn(0, 255) shl 24) or
                    ((sumR[0] / divSumSq).coerceIn(0, 255) shl 16) or
                    ((sumG[0] / divSumSq).coerceIn(0, 255) shl 8) or
                    (sumB[0] / divSumSq).coerceIn(0, 255)

            sumR[0] -= outSumR[0]; sumG[0] -= outSumG[0]
            sumB[0] -= outSumB[0]; sumA[0] -= outSumA[0]
            val si = (sp - radius + div) % div
            val sir = stack[si]
            outSumR[0] -= sir[0]; outSumG[0] -= sir[1]
            outSumB[0] -= sir[2]; outSumA[0] -= sir[3]

            if (x == 0) vMin[y] = min(y + radius + 1, h - 1)
            val idx2 = vMin[y] * w + x
            sir[0] = r[idx2]; sir[1] = g[idx2]; sir[2] = b[idx2]; sir[3] = a[idx2]
            inSumR[0] += sir[0]; inSumG[0] += sir[1]
            inSumB[0] += sir[2]; inSumA[0] += sir[3]
            sumR[0] += inSumR[0]; sumG[0] += inSumG[0]
            sumB[0] += inSumB[0]; sumA[0] += inSumA[0]

            sp = (sp + 1) % div
            val sir2 = stack[sp]
            outSumR[0] += sir2[0]; outSumG[0] += sir2[1]
            outSumB[0] += sir2[2]; outSumA[0] += sir2[3]
            inSumR[0] -= sir2[0]; inSumG[0] -= sir2[1]
            inSumB[0] -= sir2[2]; inSumA[0] -= sir2[3]
        }
    }
}
