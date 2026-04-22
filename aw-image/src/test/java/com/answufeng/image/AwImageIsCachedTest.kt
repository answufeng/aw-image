package com.answufeng.image

import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AwImageIsCachedTest {

    private lateinit var appContext: android.content.Context

    @Before
    fun setup() {
        appContext = RuntimeEnvironment.getApplication()
        AwImage.init(appContext) { }
    }

    @Test
    fun `isCached for unloaded network url does not throw and returns false`() {
        val url = "https://127.0.0.1:9/aw_image_iscache_test.png"
        assertFalse(AwImage.isCached(appContext, url))
    }

    @Test
    fun `isCached with requestConfig does not throw`() {
        val url = "https://127.0.0.1:9/aw_image_iscache_test2.png"
        assertFalse(
            AwImage.isCached(appContext, url) {
                size(100, 100)
            }
        )
    }
}
