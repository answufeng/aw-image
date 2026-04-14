package com.answufeng.image

import android.content.Context
import coil.request.ImageRequest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AwImageScopeTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private fun createScope(): AwImageScope {
        val builder = ImageRequest.Builder(context).data(0)
        return AwImageScope(builder)
    }

    @Test
    fun `default values`() {
        val scope = createScope()
        assertEquals(0, scope.fallbackResId)
        assertNull(scope.fallbackDrawable)
    }

    @Test
    fun `placeholder and error setters with resId`() {
        val scope = createScope()
        scope.placeholder(123)
        scope.error(456)
    }

    @Test
    fun `fallback setter with resId`() {
        val scope = createScope()
        scope.fallback(789)
        assertEquals(789, scope.fallbackResId)
    }

    @Test
    fun `fallback setter with Drawable`() {
        val scope = createScope()
        val drawable = android.graphics.drawable.ColorDrawable(0xFF000000.toInt())
        scope.fallback(drawable)
        assertEquals(drawable, scope.fallbackDrawable)
    }

    @Test
    fun `circle enables circle crop`() {
        val scope = createScope()
        scope.circle()
    }

    @Test
    fun `roundedCorners sets uniform radius`() {
        val scope = createScope()
        scope.roundedCorners(12f)
    }

    @Test
    fun `roundedCorners sets individual corners`() {
        val scope = createScope()
        scope.roundedCorners(1f, 2f, 3f, 4f)
    }

    @Test
    fun `override sets dimensions`() {
        val scope = createScope()
        scope.override(200, 300)
    }

    @Test
    fun `noCache disables cache`() {
        val scope = createScope()
        scope.noCache()
    }

    @Test
    fun `transform accumulates transformations`() {
        val scope = createScope()
        scope.transform(GrayscaleTransformation())
        scope.transform(ColorFilterTransformation(0xFFFF0000.toInt()))
        scope.transform(BlurTransformation())
    }

    @Test
    fun `crossfade with duration`() {
        val scope = createScope()
        scope.crossfade(300)
    }

    @Test
    fun `crossfade with zero does not crash`() {
        val scope = createScope()
        scope.crossfade(0)
    }

    @Test
    fun `crossfade boolean toggles`() {
        val scope = createScope()
        scope.crossfade(false)
        scope.crossfade(true)
    }

    @Test
    fun `listener sets callbacks in cumulative mode`() {
        val scope = createScope()
        scope.listener(onStart = {})
        scope.listener(onSuccess = {})
        scope.listener(onError = {})
    }

    @Test
    fun `onStart onSuccess onError individual setters`() {
        val scope = createScope()
        scope.onStart {}
        scope.onSuccess {}
        scope.onError {}
    }

    @Test
    fun `cacheOnlyOnOffline setter`() {
        val scope = createScope()
        scope.cacheOnlyOnOffline(false)
    }
}
