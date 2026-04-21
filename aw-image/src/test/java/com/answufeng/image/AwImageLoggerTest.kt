package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class AwImageLoggerTest {

    @Test
    fun `default enabled is false`() {
        AwImageLogger.enabled = false
        assertFalse(AwImageLogger.enabled)
    }

    @Test
    fun `enabled can be toggled`() {
        AwImageLogger.enabled = true
        assertTrue(AwImageLogger.enabled)
        AwImageLogger.enabled = false
        assertFalse(AwImageLogger.enabled)
    }

    @Test
    fun `default tag is aw-image`() {
        assertEquals("aw-image", AwImageLogger.tag)
    }

    @Test
    fun `d does not crash when disabled`() {
        AwImageLogger.enabled = false
        AwImageLogger.d("test message")
    }

    @Test
    fun `e does not crash when disabled`() {
        AwImageLogger.enabled = false
        AwImageLogger.e("test error")
        AwImageLogger.e("test error with throwable", RuntimeException("test"))
    }
}
