package com.answufeng.image

import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.transition.Transition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AwImageScopeTest {

    @Test
    fun `AwImageScope class exists`() {
        val clazz = Class.forName("com.answufeng.image.AwImageScope")
        assertNotNull(clazz)
    }

    @Test
    fun `crossfade false sets Transition Factory NONE`() {
        val context = RuntimeEnvironment.getApplication()
        val builder = ImageRequest.Builder(context).data("https://example.com/a.png")
        val scope = AwImageScope(builder)
        scope.crossfade(false)
        scope.applyTo(context)
        val request = builder.build()
        assertEquals(Transition.Factory.NONE, request.transitionFactory)
    }

    @Test
    fun `crossfade duration 0 sets Transition Factory NONE`() {
        val context = RuntimeEnvironment.getApplication()
        val builder = ImageRequest.Builder(context).data("https://example.com/b.png")
        val scope = AwImageScope(builder)
        scope.crossfade(0)
        scope.applyTo(context)
        val request = builder.build()
        assertEquals(Transition.Factory.NONE, request.transitionFactory)
    }

    @Test
    fun `addHeader reaches request`() {
        val context = RuntimeEnvironment.getApplication()
        val builder = ImageRequest.Builder(context).data("https://example.com/h.png")
        val scope = AwImageScope(builder)
        scope.addHeader("X-Test", "aw-image")
        scope.applyTo(context)
        val request = builder.build()
        assertEquals("aw-image", request.headers["X-Test"])
    }

    @Test
    fun `onProgress registers unique token and request header before applyTo`() {
        val context = RuntimeEnvironment.getApplication()
        val url = "https://example.com/progress.png"
        val builder = ImageRequest.Builder(context).data(url)
        val scope = AwImageScope(builder, url)
        scope.onProgress { _, _ -> }
        assertNull(scope.progressToken)
        scope.registerProgressIfNeeded()
        assertNotNull(scope.progressToken)
        val token = scope.progressToken!!
        scope.applyTo(context)
        val request = builder.build()
        assertEquals(token, request.headers[ProgressInterceptor.PROGRESS_TOKEN_HEADER])
    }

    @Test
    fun `raw block runs and can set networkCachePolicy`() {
        val context = RuntimeEnvironment.getApplication()
        val builder = ImageRequest.Builder(context).data("https://example.com/r.png")
        val scope = AwImageScope(builder)
        scope.raw { networkCachePolicy(CachePolicy.DISABLED) }
        scope.applyTo(context)
        val request = builder.build()
        assertEquals(CachePolicy.DISABLED, request.networkCachePolicy)
    }
}
