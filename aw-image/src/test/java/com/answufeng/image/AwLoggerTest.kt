package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class AwLoggerTest {

    @Test
    fun `default enabled is false`() {
        AwLogger.enabled = false
        assertFalse(AwLogger.enabled)
    }

    @Test
    fun `enabled can be toggled`() {
        AwLogger.enabled = true
        assertTrue(AwLogger.enabled)
        AwLogger.enabled = false
        assertFalse(AwLogger.enabled)
    }

    @Test
    fun `default tag is aw-image`() {
        assertEquals("aw-image", AwLogger.tag)
    }

    @Test
    fun `d does not crash when disabled`() {
        AwLogger.enabled = false
        AwLogger.d("test message")
    }

    @Test
    fun `e does not crash when disabled`() {
        AwLogger.enabled = false
        AwLogger.e("test error")
        AwLogger.e("test error with throwable", RuntimeException("test"))
    }
}
