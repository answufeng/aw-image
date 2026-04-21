package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class TransformationsTest {

    @Test
    fun `GrayscaleTransformation cacheKey`() {
        val t = GrayscaleTransformation()
        assertEquals("aw_grayscale", t.cacheKey)
    }

    @Test
    fun `ColorFilterTransformation cacheKey includes color`() {
        val t = ColorFilterTransformation(0x80FF0000.toInt())
        assertTrue(t.cacheKey.startsWith("aw_color_filter_"))
        assertTrue(t.cacheKey.contains("80ff0000"))
    }

    @Test
    fun `BlurTransformation cacheKey includes params`() {
        val t = BlurTransformation(15, 4)
        assertEquals("aw_blur_15_4", t.cacheKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `BlurTransformation rejects radius 0`() {
        BlurTransformation(radius = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `BlurTransformation rejects radius 26`() {
        BlurTransformation(radius = 26)
    }

    @Test
    fun `BlurTransformation accepts radius 1`() {
        val t = BlurTransformation(radius = 1)
        assertEquals("aw_blur_1_4", t.cacheKey)
    }

    @Test
    fun `BlurTransformation accepts radius 25`() {
        val t = BlurTransformation(radius = 25)
        assertEquals("aw_blur_25_4", t.cacheKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `BlurTransformation rejects sampling 0`() {
        BlurTransformation(sampling = 0)
    }

    @Test
    fun `BlurTransformation accepts sampling 1`() {
        val t = BlurTransformation(sampling = 1)
        assertEquals("aw_blur_15_1", t.cacheKey)
    }

    @Test
    fun `BorderTransformation cacheKey includes params`() {
        val t = BorderTransformation(4f, 0xFFFF0000.toInt())
        val key = t.cacheKey
        assertTrue(key.startsWith("aw_border_4.0_"))
        assertTrue(key.endsWith("_false"))
    }

    @Test
    fun `BorderTransformation circle cacheKey`() {
        val t = BorderTransformation(4f, 0xFFFF0000.toInt(), circle = true)
        val key = t.cacheKey
        assertTrue(key.startsWith("aw_border_4.0_"))
        assertTrue(key.endsWith("_true"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `BorderTransformation rejects zero width`() {
        BorderTransformation(borderWidth = 0f, borderColor = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `BorderTransformation rejects negative width`() {
        BorderTransformation(borderWidth = -1f, borderColor = 0)
    }

    @Test
    fun `CropTransformation cacheKey includes params`() {
        val t = CropTransformation(10, 20, 100, 200)
        assertEquals("aw_crop_10_20_100_200", t.cacheKey)
    }

    @Test
    fun `CropTransformation default values`() {
        val t = CropTransformation()
        assertEquals("aw_crop_0_0_0_0", t.cacheKey)
    }

    @Test
    fun `WatermarkTransformation cacheKey includes params`() {
        val bitmap = android.graphics.Bitmap.createBitmap(10, 10, android.graphics.Bitmap.Config.ARGB_8888)
        val t = WatermarkTransformation(bitmap, 5, 10, 128)
        assertTrue(t.cacheKey.startsWith("aw_watermark_"))
        assertTrue(t.cacheKey.contains("_5_10_128"))
        bitmap.recycle()
    }
}
