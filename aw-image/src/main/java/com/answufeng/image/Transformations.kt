package com.answufeng.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import coil.size.Size
import coil.transform.Transformation
import kotlin.math.min

/**
 * 灰度变换，将图片转换为灰度效果。
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
 * 颜色滤镜变换，在图片上叠加指定颜色。
 *
 * @param color 叠加的颜色值（ARGB）
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(ColorFilterTransformation(Color.parseColor("#80FF0000")))
 * }
 * ```
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
 * 边框变换，为图片添加边框。支持圆形和矩形两种模式。
 *
 * 当 [circle] 为 `true` 时，会先将图片裁切为圆形，再绘制圆形边框。
 *
 * @param borderWidth 边框宽度（像素）
 * @param borderColor 边框颜色
 * @param circle      是否使用圆形模式（默认 false，矩形边框）
 *
 * ```kotlin
 * // 矩形边框
 * imageView.loadImage(url) {
 *     transform(BorderTransformation(2f, Color.WHITE))
 * }
 *
 * // 圆形边框（推荐使用 loadCircleWithBorder 快捷方法）
 * imageView.loadCircleWithBorder(url, borderWidth = 4f, borderColor = Color.WHITE)
 * ```
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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
        }
        if (circle) {
            val cx = input.width / 2f
            val cy = input.height / 2f
            val maxRadius = minOf(input.width, input.height) / 2f
            val radius = (maxRadius - borderWidth / 2f).coerceAtLeast(0f)
            val clipPath = Path().apply {
                addCircle(cx, cy, maxRadius, Path.Direction.CW)
            }
            canvas.clipPath(clipPath)
            canvas.drawBitmap(input, 0f, 0f, null)
            if (radius > 0f) {
                canvas.drawCircle(cx, cy, radius, paint)
            }
        } else {
            canvas.drawBitmap(input, 0f, 0f, null)
            val half = borderWidth / 2f
            val right = (input.width - half).coerceAtLeast(half)
            val bottom = (input.height - half).coerceAtLeast(half)
            canvas.drawRect(half, half, right, bottom, paint)
        }
        return output
    }
}

/**
 * 高斯模糊变换。
 *
 * 在 Android 12+ (API 31) 上使用 [RenderEffect] 硬件加速模糊，
 * 低版本回退到 StackBlur 纯软件实现。
 *
 * @param radius   模糊半径（像素），范围 1~25（StackBlur 上限），RenderEffect 无此限制
 * @param sampling 采样率（默认 1），大于 1 时先缩小再模糊再放大，可显著提升性能
 *
 * ```kotlin
 * // 普通模糊
 * imageView.loadBlur(url)
 *
 * // 自定义模糊半径
 * imageView.loadBlur(url, radius = 20)
 *
 * // 高性能模糊（先缩小 2 倍再模糊）
 * imageView.loadBlur(url, radius = 25, sampling = 2)
 * ```
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

/**
 * 裁切变换，从图片中裁切指定区域。
 *
 * @param x      裁切起始 X 坐标（像素）
 * @param y      裁切起始 Y 坐标（像素）
 * @param width  裁切宽度（像素），0 表示裁切到图片右边缘
 * @param height 裁切高度（像素），0 表示裁切到图片下边缘
 *
 * ```kotlin
 * imageView.loadImage(url) {
 *     transform(CropTransformation(0, 0, 200, 200))
 * }
 * ```
 */
class CropTransformation(
    private val x: Int = 0,
    private val y: Int = 0,
    private val width: Int = 0,
    private val height: Int = 0
) : Transformation {
    override val cacheKey = "aw_crop_${x}_${y}_${width}_${height}"
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val cropX = x.coerceIn(0, input.width)
        val cropY = y.coerceIn(0, input.height)
        val cropW = if (width <= 0) input.width - cropX else minOf(width, input.width - cropX)
        val cropH = if (height <= 0) input.height - cropY else minOf(height, input.height - cropY)
        if (cropW <= 0 || cropH <= 0) return input
        return Bitmap.createBitmap(input, cropX, cropY, cropW, cropH)
    }
}

/**
 * 水印变换，在图片上叠加水印图片。
 *
 * @param watermark 水印 Bitmap
 * @param x         水印 X 坐标（像素），默认 0
 * @param y         水印 Y 坐标（像素），默认 0
 * @param alpha     水印透明度（0~255），默认 128
 *
 * ```kotlin
 * val watermark = BitmapFactory.decodeResource(resources, R.drawable.watermark)
 * imageView.loadImage(url) {
 *     transform(WatermarkTransformation(watermark, alpha = 100))
 * }
 * ```
 */
class WatermarkTransformation(
    private val watermark: Bitmap,
    private val x: Int = 0,
    private val y: Int = 0,
    private val alpha: Int = 128
) : Transformation {
    override val cacheKey = "aw_watermark_${watermark.generationId}_${x}_${y}_${alpha}"
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val config = input.config ?: Bitmap.Config.ARGB_8888
        val output = Bitmap.createBitmap(input.width, input.height, config)
        val canvas = Canvas(output)
        canvas.drawBitmap(input, 0f, 0f, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.alpha = alpha.coerceIn(0, 255)
        }
        canvas.drawBitmap(watermark, x.toFloat(), y.toFloat(), paint)
        return output
    }
}

internal object RenderEffectBlur {

    fun apply(input: Bitmap, radius: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        return runCatching {
            val blurEffect = RenderEffect.createBlurEffect(
                radius.toFloat(), radius.toFloat(), Shader.TileMode.CLAMP
            )
            val config = input.config ?: Bitmap.Config.ARGB_8888
            val output = Bitmap.createBitmap(input.width, input.height, config)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.setRenderEffect(blurEffect)
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
