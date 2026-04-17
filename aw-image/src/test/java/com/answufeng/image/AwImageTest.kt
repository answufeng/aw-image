package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class AwImageTest {

    @Test
    fun `ImageConfig default values`() {
        val config = AwImage.ImageConfig()
        assertEquals(0.25, config.memoryCachePercent, 0.001)
        assertEquals(100L * 1024 * 1024, config.diskCacheSize)
        assertTrue(config.crossfadeEnabled)
        assertEquals(200, config.crossfadeDuration)
        assertTrue(config.gifEnabled)
        assertEquals(0, config.placeholderRes)
        assertNull(config.placeholderDrawable)
        assertEquals(0, config.errorRes)
        assertNull(config.errorDrawable)
        assertEquals(0, config.fallbackRes)
        assertNull(config.fallbackDrawable)
        assertNull(config.memoryCacheMaxBytes)
        assertNull(config.diskCacheDir)
        assertNull(config.okHttpClient)
    }

    @Test
    fun `ImageConfig memoryCacheSize clamps to valid range`() {
        val config = AwImage.ImageConfig()
        config.memoryCacheSize(0.01)
        assertEquals(0.05, config.memoryCachePercent, 0.001)
        config.memoryCacheSize(0.8)
        assertEquals(0.5, config.memoryCachePercent, 0.001)
        config.memoryCacheSize(0.3)
        assertEquals(0.3, config.memoryCachePercent, 0.001)
    }

    @Test
    fun `ImageConfig diskCacheSize coerces to at least 0`() {
        val config = AwImage.ImageConfig()
        config.diskCacheSize(-100)
        assertEquals(0L, config.diskCacheSize)
        config.diskCacheSize(256L * 1024 * 1024)
        assertEquals(256L * 1024 * 1024, config.diskCacheSize)
    }

    @Test
    fun `ImageConfig enableGif toggles`() {
        val config = AwImage.ImageConfig()
        assertTrue(config.gifEnabled)
        config.enableGif(false)
        assertFalse(config.gifEnabled)
    }

    @Test
    fun `ImageConfig placeholder and error setters with resId`() {
        val config = AwImage.ImageConfig()
        config.placeholder(123)
        assertEquals(123, config.placeholderRes)
        config.error(456)
        assertEquals(456, config.errorRes)
    }

    @Test
    fun `ImageConfig placeholder and error setters with Drawable`() {
        val config = AwImage.ImageConfig()
        val drawable = android.graphics.drawable.ColorDrawable(0xFF000000.toInt())
        config.placeholder(drawable)
        assertEquals(drawable, config.placeholderDrawable)
        config.error(drawable)
        assertEquals(drawable, config.errorDrawable)
    }

    @Test
    fun `ImageConfig fallback setters`() {
        val config = AwImage.ImageConfig()
        config.fallback(789)
        assertEquals(789, config.fallbackRes)
        val drawable = android.graphics.drawable.ColorDrawable(0xFF000000.toInt())
        config.fallback(drawable)
        assertEquals(drawable, config.fallbackDrawable)
    }

    @Test
    fun `ImageConfig crossfade setters`() {
        val config = AwImage.ImageConfig()
        config.crossfade(false)
        assertFalse(config.crossfadeEnabled)
        config.crossfade(true)
        assertTrue(config.crossfadeEnabled)
        config.crossfade(500)
        assertEquals(500, config.crossfadeDuration)
        config.crossfade(-1)
        assertEquals(0, config.crossfadeDuration)
    }

    @Test
    fun `ImageConfig diskCacheDir setter`() {
        val config = AwImage.ImageConfig()
        val dir = java.io.File("/tmp/test_cache")
        config.diskCacheDir(dir)
        assertEquals(dir, config.diskCacheDir)
    }

    @Test
    fun `ImageConfig okHttpClient setter`() {
        val config = AwImage.ImageConfig()
        val client = okhttp3.OkHttpClient.Builder().build()
        config.okHttpClient(client)
        assertEquals(client, config.okHttpClient)
    }

    @Test
    fun `isInitialized defaults to false`() {
        assertFalse(AwImage.isInitialized)
    }

    @Test
    fun `globalPlaceholder and globalError default to 0`() {
        assertEquals(0, AwImage.globalPlaceholder)
        assertEquals(0, AwImage.globalError)
    }

    @Test
    fun `globalPlaceholderDrawable and globalErrorDrawable default to null`() {
        assertNull(AwImage.globalPlaceholderDrawable)
        assertNull(AwImage.globalErrorDrawable)
    }

    @Test
    fun `globalFallback and globalFallbackDrawable default to 0 and null`() {
        assertEquals(0, AwImage.globalFallback)
        assertNull(AwImage.globalFallbackDrawable)
    }
}
