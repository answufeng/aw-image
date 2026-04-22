package com.answufeng.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AwImageConfigTest {

    @Test
    fun `global crossfade duration 0 disables crossfade`() {
        val c = AwImage.ImageConfig()
        c.crossfade(200)
        assertTrue(c.crossfadeEnabled)
        c.crossfade(0)
        assertFalse(c.crossfadeEnabled)
        assertEquals(0, c.crossfadeDuration)
    }
}
