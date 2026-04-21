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
 * 图片颜色滤镜转换。
 *
 * Coil [Transformation] 实现，使用 [PorterDuff.Mode.SRC_ATOP] 混合模式为图片叠加颜色。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(ColorFilterTransformation(Color.argb(128, 0, 0, 0)))
 * }
 * ```
 *
 * @param color ARGB 颜色值
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
 * 图片边框转换。
 *
 * Coil [Transformation] 实现，为图片添加边框，支持圆形和矩形模式。
 *
 * ```kotlin
 * // 圆形带白色边框
 * imageView.loadImage(url) {
 *     transform(BorderTransformation(4f, Color.WHITE, circle = true))
 * }
 *
 * // 圆角矩形带黑色边框
 * imageView.loadImage(url) {
 *     transform(BorderTransformation(8f, Color.BLACK, circle = false))
 * }
 * ```
 *
 * @param borderWidth 边框宽度（px），必须 > 0
 * @param borderColor 边框颜色
 * @param circle      true 为圆形边框，false 为矩形边框
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
 * 图片高斯模糊转换。
 *
 * Coil [Transformation] 实现，支持 RenderEffect 硬件加速模糊和 StackBlur 软件模糊。
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(BlurTransformation(radius = 15, sampling = 4))
 * }
 * ```
 *
 * @param radius   模糊半径（1~25），默认 15。值越大越模糊，但性能消耗也越大
 * @param sampling 采样因子（≥1），默认 4。值越大模糊效果越快，但质量会有所下降
 * @throws IllegalArgumentException 如果 [radius] 不在 1..25 范围内，或 [sampling] < 1
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
            val result = RenderEffectBlur.apply(input, radius)
            if (result != null) return result
        }

        return applyStackBlur(input)
    }

    private fun applyStackBlur(input: Bitmap): Bitmap {
        val scaledWidth = (input.width / sampling).coerceAtLeast(1)
        val scaledHeight = (input.height / sampling).coerceAtLeast(1)

        val scaledBitmap = Bitmap.createScaledBitmap(input, scaledWidth, scaledHeight, true)

        val blurred = StackBlur.blur(scaledBitmap, radius)
        if (scaledBitmap !== input) scaledBitmap.recycle()

        if (blurred.width == input.width && blurred.height == input.height) return blurred
        val output = Bitmap.createScaledBitmap(blurred, input.width, input.height, true)
        if (output !== blurred) blurred.recycle()
        return output
    }
}

internal object RenderEffectBlur {

    private val createBlurEffect by lazy {
        runCatching {
            val renderEffectClass = Class.forName("android.graphics.RenderEffect")
            val shaderClass = Class.forName("android.graphics.Shader")
            val tileModeClass = Class.forName("android.graphics.Shader\$TileMode")
            val clamp = tileModeClass.enumConstants?.firstOrNull {
                (it as? Enum<*>)?.name == "CLAMP"
            } ?: return@runCatching null
            renderEffectClass.getMethod(
                "createBlurEffect",
                Float::class.javaPrimitiveType,
                Float::class.javaPrimitiveType,
                shaderClass,
                tileModeClass
            )
        }.getOrNull()
    }

    private val setRenderEffect by lazy {
        runCatching {
            val renderEffectClass = Class.forName("android.graphics.RenderEffect")
            Paint::class.java.getMethod("setRenderEffect", renderEffectClass)
        }.getOrNull()
    }

    fun apply(input: Bitmap, radius: Int): Bitmap? {
        val createMethod = createBlurEffect ?: return null
        val setMethod = setRenderEffect ?: return null
        return runCatching {
            val blurEffect = createMethod.invoke(null, radius.toFloat(), radius.toFloat(), null, null)
                ?: return null
            val config = input.config ?: Bitmap.Config.ARGB_8888
            val output = Bitmap.createBitmap(input.width, input.height, config)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            setMethod.invoke(paint, blurEffect)
            canvas.drawBitmap(input, 0f, 0f, paint)
            output
        }.onFailure {
            AwLogger.e("RenderEffectBlur: failed, falling back to StackBlur", it)
        }.getOrNull()
    }
}

internal object StackBlur {

    private val bufferHolder = object : ThreadLocal<IntArray>() {
        override fun initialValue(): IntArray = IntArray(0)
    }

    private fun getBuffer(minSize: Int): IntArray {
        val buf = bufferHolder.get() ?: IntArray(0)
        if (buf.size >= minSize) return buf
        val newBuf = IntArray(minSize)
        bufferHolder.set(newBuf)
        return newBuf
    }

    fun blur(bitmap: Bitmap, radius: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return bitmap

        val size = w * h
        val pixels = IntArray(size)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val div = 2 * radius + 1
        val divSum = (div + 1) shr 1
        val divSumSq = divSum * divSum

        val vMin = getBuffer(maxOf(w, h))

        horizontalPass(pixels, vMin, w, h, radius, div, divSumSq)

        verticalPass(pixels, vMin, w, h, radius, div, divSumSq)

        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(w, h, config).also {
            it.setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    private fun horizontalPass(
        pixels: IntArray, vMin: IntArray, w: Int, h: Int,
        radius: Int, div: Int, divSumSq: Int,
    ) {
        val stack = Array(div) { IntArray(4) }

        for (y in 0 until h) {
            var sumR = 0; var sumG = 0; var sumB = 0; var sumA = 0
            var inSumR = 0; var inSumG = 0; var inSumB = 0; var inSumA = 0
            var outSumR = 0; var outSumG = 0; var outSumB = 0; var outSumA = 0

            for (i in -radius..radius) {
                val p = pixels[y * w + min(w - 1, maxOf(i, 0))]
                val sir = stack[i + radius]
                sir[0] = (p shr 16) and 0xff
                sir[1] = (p shr 8) and 0xff
                sir[2] = p and 0xff
                sir[3] = (p ushr 24) and 0xff
                val weight = radius + 1 - kotlin.math.abs(i)
                sumR += sir[0] * weight; sumG += sir[1] * weight
                sumB += sir[2] * weight; sumA += sir[3] * weight
                if (i > 0) {
                    inSumR += sir[0]; inSumG += sir[1]
                    inSumB += sir[2]; inSumA += sir[3]
                } else {
                    outSumR += sir[0]; outSumG += sir[1]
                    outSumB += sir[2]; outSumA += sir[3]
                }
            }

            var sp = radius
            for (x in 0 until w) {
                val idx = y * w + x
                pixels[idx] = ((sumA / divSumSq).coerceIn(0, 255) shl 24) or
                        ((sumR / divSumSq).coerceIn(0, 255) shl 16) or
                        ((sumG / divSumSq).coerceIn(0, 255) shl 8) or
                        (sumB / divSumSq).coerceIn(0, 255)

                sumR -= outSumR; sumG -= outSumG
                sumB -= outSumB; sumA -= outSumA

                val si = (sp - radius + div) % div
                val sir = stack[si]
                outSumR -= sir[0]; outSumG -= sir[1]
                outSumB -= sir[2]; outSumA -= sir[3]

                if (y == 0) vMin[x] = min(x + radius + 1, w - 1)
                val p = pixels[y * w + vMin[x]]
                sir[0] = (p shr 16) and 0xff; sir[1] = (p shr 8) and 0xff
                sir[2] = p and 0xff; sir[3] = (p ushr 24) and 0xff
                inSumR += sir[0]; inSumG += sir[1]
                inSumB += sir[2]; inSumA += sir[3]
                sumR += inSumR; sumG += inSumG
                sumB += inSumB; sumA += inSumA

                sp = (sp + 1) % div
                val sir2 = stack[sp]
                outSumR += sir2[0]; outSumG += sir2[1]
                outSumB += sir2[2]; outSumA += sir2[3]
                inSumR -= sir2[0]; inSumG -= sir2[1]
                inSumB -= sir2[2]; inSumA -= sir2[3]
            }
        }
    }

    private fun verticalPass(
        pixels: IntArray, vMin: IntArray, w: Int, h: Int,
        radius: Int, div: Int, divSumSq: Int,
    ) {
        val stack = Array(div) { IntArray(4) }

        for (x in 0 until w) {
            var sumR = 0; var sumG = 0; var sumB = 0; var sumA = 0
            var inSumR = 0; var inSumG = 0; var inSumB = 0; var inSumA = 0
            var outSumR = 0; var outSumG = 0; var outSumB = 0; var outSumA = 0

            for (i in -radius..radius) {
                val yp = min(h - 1, maxOf(i, 0))
                val idx = yp * w + x
                val p = pixels[idx]
                val sir = stack[i + radius]
                sir[0] = (p shr 16) and 0xff; sir[1] = (p shr 8) and 0xff
                sir[2] = p and 0xff; sir[3] = (p ushr 24) and 0xff
                val weight = radius + 1 - kotlin.math.abs(i)
                sumR += sir[0] * weight; sumG += sir[1] * weight
                sumB += sir[2] * weight; sumA += sir[3] * weight
                if (i > 0) {
                    inSumR += sir[0]; inSumG += sir[1]
                    inSumB += sir[2]; inSumA += sir[3]
                } else {
                    outSumR += sir[0]; outSumG += sir[1]
                    outSumB += sir[2]; outSumA += sir[3]
                }
            }

            var sp = radius
            for (y in 0 until h) {
                val idx = y * w + x
                pixels[idx] = ((sumA / divSumSq).coerceIn(0, 255) shl 24) or
                        ((sumR / divSumSq).coerceIn(0, 255) shl 16) or
                        ((sumG / divSumSq).coerceIn(0, 255) shl 8) or
                        (sumB / divSumSq).coerceIn(0, 255)

                sumR -= outSumR; sumG -= outSumG
                sumB -= outSumB; sumA -= outSumA
                val si = (sp - radius + div) % div
                val sir = stack[si]
                outSumR -= sir[0]; outSumG -= sir[1]
                outSumB -= sir[2]; outSumA -= sir[3]

                if (x == 0) vMin[y] = min(y + radius + 1, h - 1)
                val idx2 = vMin[y] * w + x
                val p = pixels[idx2]
                sir[0] = (p shr 16) and 0xff; sir[1] = (p shr 8) and 0xff
                sir[2] = p and 0xff; sir[3] = (p ushr 24) and 0xff
                inSumR += sir[0]; inSumG += sir[1]
                inSumB += sir[2]; inSumA += sir[3]
                sumR += inSumR; sumG += inSumG
                sumB += inSumB; sumA += inSumA

                sp = (sp + 1) % div
                val sir2 = stack[sp]
                outSumR += sir2[0]; outSumG += sir2[1]
                outSumB += sir2[2]; outSumA += sir2[3]
                inSumR -= sir2[0]; inSumG -= sir2[1]
                inSumB -= sir2[2]; inSumA -= sir2[3]
            }
        }
    }
}
