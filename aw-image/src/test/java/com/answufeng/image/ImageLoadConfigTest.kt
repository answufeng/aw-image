package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class ImageLoadConfigTest {

    @Test
    fun `default values`() {
        val config = ImageLoadConfig()
        assertEquals(0, config.placeholderRes)
        assertEquals(0, config.errorRes)
        assertEquals(coil.size.Scale.FIT, config.scale)
        assertEquals(0, config.overrideWidth)
        assertEquals(0, config.overrideHeight)
        assertFalse(config.isCircle)
        assertEquals(0f, config.cornerRadius, 0.001f)
        assertTrue(config.cacheEnabled)
        assertTrue(config.crossfade)
        assertEquals(0, config.crossfadeDuration)
    }

    @Test
    fun `placeholder and error setters`() {
        val config = ImageLoadConfig()
        config.placeholder(R_test.drawable_res)
        assertEquals(R_test.drawable_res, config.placeholderRes)
        config.error(R_test.drawable_res)
        assertEquals(R_test.drawable_res, config.errorRes)
    }

    @Test
    fun `circle enables circle crop`() {
        val config = ImageLoadConfig()
        config.circle()
        assertTrue(config.isCircle)
    }

    @Test
    fun `roundedCorners sets uniform radius`() {
        val config = ImageLoadConfig()
        config.roundedCorners(12f)
        assertEquals(12f, config.cornerRadius, 0.001f)
    }

    @Test
    fun `roundedCorners sets individual corners`() {
        val config = ImageLoadConfig()
        config.roundedCorners(1f, 2f, 3f, 4f)
        assertEquals(1f, config.cornerTopLeft, 0.001f)
        assertEquals(2f, config.cornerTopRight, 0.001f)
        assertEquals(3f, config.cornerBottomRight, 0.001f)
        assertEquals(4f, config.cornerBottomLeft, 0.001f)
        assertEquals(4f, config.cornerRadius, 0.001f)
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
    fun `transform sets custom transformations`() {
        val config = ImageLoadConfig()
        config.transform(GrayscaleTransformation())
        assertNotNull(config.customTransformations)
        assertEquals(1, config.customTransformations!!.size)
    }

    @Test
    fun `crossfade with duration`() {
        val config = ImageLoadConfig()
        config.crossfade(300)
        assertEquals(300, config.crossfadeDuration)
        assertTrue(config.crossfade)
    }

    @Test
    fun `crossfade with zero disables`() {
        val config = ImageLoadConfig()
        config.crossfade(0)
        assertEquals(0, config.crossfadeDuration)
        assertFalse(config.crossfade)
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
}

private object R_test {
    const val drawable_res = 12345
}
