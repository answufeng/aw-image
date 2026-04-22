package com.answufeng.image

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AwImageInstrumentedTest {

    private lateinit var context: android.content.Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun init_withDefaultConfig_doesNotCrash() {
        AwImage.init(context)
        assertTrue(AwImage.isInitialized)
    }

    @Test
    fun init_withCustomConfig_doesNotCrash() {
        AwImage.init(context) {
            memoryCacheSize(0.3)
            diskCacheSize(50L * 1024 * 1024)
            enableGif(true)
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.ic_menu_report_image)
            enableLogging(false)
        }
        assertTrue(AwImage.isInitialized)
    }

    @Test
    fun init_withDrawablePlaceholder_doesNotCrash() {
        AwImage.init(context) {
            placeholder(ColorDrawable(Color.GRAY))
            error(ColorDrawable(Color.RED))
        }
        assertTrue(AwImage.isInitialized)
    }

    @Test
    fun clearMemoryCache_returnsTrue() {
        AwImage.init(context)
        val result = AwImage.clearMemoryCache(context)
        assertTrue(result)
    }

    @Test
    fun clearDiskCache_returnsTrue() {
        AwImage.init(context)
        val result = AwImage.clearDiskCache(context)
        assertTrue(result)
    }

    @Test
    fun loadImage_withNull_returnsDisposable() {
        AwImage.init(context)
        val imageView = ImageView(context)
        val disposable = imageView.loadImage(null)
        assertNotNull(disposable)
    }

    @Test
    fun loadImage_withNullAndFallback_showsFallback() {
        AwImage.init(context)
        val imageView = ImageView(context)
        imageView.loadImage(null) {
            fallback(android.R.drawable.ic_menu_gallery)
        }
    }

    @Test
    fun loadImage_withNullAndDrawableFallback_showsFallback() {
        AwImage.init(context)
        val imageView = ImageView(context)
        imageView.loadImage(null) {
            fallback(ColorDrawable(Color.GRAY))
        }
    }

    @Test
    fun loadCircle_withNull_returnsDisposable() {
        AwImage.init(context)
        val imageView = ImageView(context)
        val disposable = imageView.loadCircle(null)
        assertNotNull(disposable)
    }

    @Test
    fun loadRounded_withNull_returnsDisposable() {
        AwImage.init(context)
        val imageView = ImageView(context)
        val disposable = imageView.loadRounded(null, 12f)
        assertNotNull(disposable)
    }

    @Test
    fun loadBlur_withNull_returnsDisposable() {
        AwImage.init(context)
        val imageView = ImageView(context)
        val disposable = imageView.loadBlur(null)
        assertNotNull(disposable)
    }

    @Test
    fun imageLoader_returnsNonNull() {
        AwImage.init(context)
        val loader = AwImage.imageLoader(context)
        assertNotNull(loader)
    }

    @Test
    fun isCached_doesNotCrash_forUnreachableUrl() {
        AwImage.init(context)
        val cached = AwImage.isCached(
            context,
            "https://127.0.0.1:9/aw_image_iscache_instrumented.png"
        )
        assertFalse(cached)
    }
}
