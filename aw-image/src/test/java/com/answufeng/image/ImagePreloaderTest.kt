package com.answufeng.image

import org.junit.Assert.*
import org.junit.Test

class ImagePreloaderTest {

    @Test
    fun `preloadAll method exists and returns List`() {
        val methods = ImagePreloader::class.java.declaredMethods
        val preloadAll = methods.firstOrNull { it.name == "preloadAll" }
        assertNotNull(preloadAll)
        val returnType = preloadAll!!.returnType
        assertTrue(List::class.java.isAssignableFrom(returnType))
    }

    @Test
    fun `preload method exists`() {
        val methods = ImagePreloader::class.java.declaredMethods
        val preload = methods.firstOrNull { it.name == "preload" }
        assertNotNull(preload)
    }

    @Test
    fun `get method exists`() {
        val methods = ImagePreloader::class.java.declaredMethods
        val get = methods.firstOrNull { it.name == "get" }
        assertNotNull(get)
    }
}
