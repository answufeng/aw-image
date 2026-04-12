package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class ImageLoadConfigTest {

    @Test
    fun `default values`() {
        val config = ImageLoadConfig()
        assertEquals(0, config.placeholderRes)
        assertEquals(0, config.errorRes)
        assertEquals(0, config.fallbackRes)
        assertEquals(coil.size.Scale.FIT, config.scale)
        assertEquals(0, config.overrideWidth)
        assertEquals(0, config.overrideHeight)
        assertFalse(config.isCircle)
        assertFalse(config.hasRoundedCorners)
        assertEquals(0f, config.cornerRadius, 0.001f)
        assertTrue(config.cacheEnabled)
        assertTrue(config.crossfadeEnabled)
        assertEquals(0, config.crossfadeDuration)
    }

    @Test
    fun `placeholder and error setters`() {
        val config = ImageLoadConfig()
        config.placeholder(123)
        assertEquals(123, config.placeholderRes)
        config.error(456)
        assertEquals(456, config.errorRes)
    }

    @Test
    fun `fallback setter`() {
        val config = ImageLoadConfig()
        config.fallback(789)
        assertEquals(789, config.fallbackRes)
    }

    @Test
    fun `scale setter`() {
        val config = ImageLoadConfig()
        config.scale(coil.size.Scale.FILL)
        assertEquals(coil.size.Scale.FILL, config.scale)
    }

    @Test
    fun `circle enables circle crop`() {
        val config = ImageLoadConfig()
        config.circle()
        assertTrue(config.isCircle)
    }

    @Test
    fun `roundedCorners sets uniform radius and flag`() {
        val config = ImageLoadConfig()
        config.roundedCorners(12f)
        assertEquals(12f, config.cornerRadius, 0.001f)
        assertTrue(config.hasRoundedCorners)
    }

    @Test
    fun `roundedCorners sets individual corners and flag`() {
        val config = ImageLoadConfig()
        config.roundedCorners(1f, 2f, 3f, 4f)
        assertEquals(1f, config.cornerTopLeft, 0.001f)
        assertEquals(2f, config.cornerTopRight, 0.001f)
        assertEquals(3f, config.cornerBottomRight, 0.001f)
        assertEquals(4f, config.cornerBottomLeft, 0.001f)
        assertTrue(config.hasRoundedCorners)
    }

    @Test
    fun `override sets dimensions`() {
        val config = ImageLoadConfig()
        config.override(200, 300)
        assertEquals(200, config.overrideWidth)
        assertEquals(300, config.overrideHeight)
    }

    @Test
    fun `noCache disables cache`() {
        val config = ImageLoadConfig()
        config.noCache()
        assertFalse(config.cacheEnabled)
    }

    @Test
    fun `transform accumulates transformations`() {
        val config = ImageLoadConfig()
        config.transform(GrayscaleTransformation())
        assertEquals(1, config.customTransformations!!.size)
        config.transform(ColorFilterTransformation(0xFFFF0000.toInt()))
        assertEquals(2, config.customTransformations!!.size)
        config.transform(BlurTransformation())
        assertEquals(3, config.customTransformations!!.size)
    }

    @Test
    fun `crossfade with duration enables crossfade`() {
        val config = ImageLoadConfig()
        config.crossfade(300)
        assertEquals(300, config.crossfadeDuration)
        assertTrue(config.crossfadeEnabled)
    }

    @Test
    fun `crossfade with zero does not enable crossfade`() {
        val config = ImageLoadConfig()
        config.crossfade(false)
        config.crossfade(0)
        assertEquals(0, config.crossfadeDuration)
        assertFalse(config.crossfadeEnabled)
    }

    @Test
    fun `crossfade boolean toggles`() {
        val config = ImageLoadConfig()
        config.crossfade(false)
        assertFalse(config.crossfadeEnabled)
        config.crossfade(true)
        assertTrue(config.crossfadeEnabled)
    }

    @Test
    fun `listener sets callbacks`() {
        val config = ImageLoadConfig()
        config.listener(
            onStart = {},
            onSuccess = {},
            onError = {}
        )
        assertNotNull(config.onStart)
        assertNotNull(config.onSuccess)
        assertNotNull(config.onError)
    }

    @Test
    fun `circle and roundedCorners interaction - circle takes precedence`() {
        val config = ImageLoadConfig()
        config.roundedCorners(12f)
        config.circle()
        assertTrue(config.isCircle)
        assertTrue(config.hasRoundedCorners)
    }
}
