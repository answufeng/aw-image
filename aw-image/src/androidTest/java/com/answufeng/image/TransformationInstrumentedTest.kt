package com.answufeng.image

import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransformationInstrumentedTest {

    private fun createTestBitmap(
        width: Int = 100,
        height: Int = 100,
        color: Int = Color.RED
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }

    @Test
    fun grayscaleTransformation_producesGrayOutput() = runBlocking {
        val input = createTestBitmap(color = Color.RED)
        val transform = GrayscaleTransformation()
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)
        assertNotSame(input, output)
        assertEquals(input.width, output.width)
        assertEquals(input.height, output.height)

        val pixel = output.getPixel(50, 50)
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        assertEquals(r, g)
        assertEquals(g, b)

        input.recycle()
        output.recycle()
    }

    @Test
    fun colorFilterTransformation_appliesColor() = runBlocking {
        val input = createTestBitmap(color = Color.WHITE)
        val transform = ColorFilterTransformation(0x80FF0000.toInt())
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)
        assertEquals(input.width, output.width)
        assertEquals(input.height, output.height)

        val pixel = output.getPixel(50, 50)
        assertTrue(Color.red(pixel) > 0)

        input.recycle()
        output.recycle()
    }

    @Test
    fun borderTransformation_rectangle_drawsBorder() = runBlocking {
        val input = createTestBitmap(color = Color.WHITE)
        val transform = BorderTransformation(4f, Color.RED)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)
        assertEquals(input.width, output.width)
        assertEquals(input.height, output.height)

        val borderPixel = output.getPixel(2, 50)
        assertEquals(Color.RED, borderPixel)

        input.recycle()
        output.recycle()
    }

    @Test
    fun borderTransformation_circle_drawsBorder() = runBlocking {
        val input = createTestBitmap(color = Color.WHITE)
        val transform = BorderTransformation(4f, Color.RED, circle = true)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)
        assertEquals(input.width, output.width)
        assertEquals(input.height, output.height)

        input.recycle()
        output.recycle()
    }

    @Test
    fun blurTransformation_producesBlurredOutput() = runBlocking {
        val input = createTestBitmap(color = Color.RED)
        val transform = BlurTransformation(5, 2)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)
        assertEquals(input.width, output.width)
        assertEquals(input.height, output.height)

        input.recycle()
        output.recycle()
    }

    @Test
    fun blurTransformation_withSampling1_producesSameSize() = runBlocking {
        val input = createTestBitmap(50, 50, Color.BLUE)
        val transform = BlurTransformation(5, 1)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)
        assertEquals(50, output.width)
        assertEquals(50, output.height)

        input.recycle()
        output.recycle()
    }

    @Test
    fun borderTransformation_wideBorder_doesNotCrash() = runBlocking {
        val input = createTestBitmap(20, 20, Color.WHITE)
        val transform = BorderTransformation(15f, Color.RED)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)

        input.recycle()
        output.recycle()
    }

    @Test
    fun borderTransformation_circleWideBorder_doesNotCrash() = runBlocking {
        val input = createTestBitmap(20, 20, Color.WHITE)
        val transform = BorderTransformation(15f, Color.RED, circle = true)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)

        input.recycle()
        output.recycle()
    }

    @Test
    fun blurTransformation_smallBitmap_doesNotCrash() = runBlocking {
        val input = createTestBitmap(2, 2, Color.GREEN)
        val transform = BlurTransformation(1, 1)
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        assertNotNull(output)

        input.recycle()
        output.recycle()
    }

    @Test
    fun grayscaleTransformation_preservesAlpha() = runBlocking {
        val input = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        input.eraseColor(Color.argb(128, 255, 0, 0))
        val transform = GrayscaleTransformation()
        val output = transform.transform(input, coil.size.Size.ORIGINAL)

        val pixel = output.getPixel(50, 50)
        val alpha = Color.alpha(pixel)
        assertTrue(alpha > 0)

        input.recycle()
        output.recycle()
    }
}
