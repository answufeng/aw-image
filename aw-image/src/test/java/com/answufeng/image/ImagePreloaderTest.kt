package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class ImagePreloaderTest {

    @Test
    fun `ImagePreloader is an object singleton`() {
        val instance = ImagePreloader
        assertNotNull(instance)
    }
}
