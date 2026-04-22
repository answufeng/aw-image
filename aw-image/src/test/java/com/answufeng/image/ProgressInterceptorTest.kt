package com.answufeng.image

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertNull
import org.junit.Test

class ProgressInterceptorTest {

    @Test
    fun `progress token header is stripped before the request reaches the server`() {
        val server = MockWebServer()
        server.enqueue(MockResponse().setBody("ok"))
        server.start()
        try {
            val client = OkHttpClient.Builder()
                .addInterceptor(ProgressInterceptor)
                .build()
            val url = server.url("/image.png")
            val token = "tok-abc"
            val req = Request.Builder()
                .url(url)
                .header(ProgressInterceptor.PROGRESS_TOKEN_HEADER, token)
                .build()
            client.newCall(req).execute().use { it.body?.string() }
            val recorded = server.takeRequest()
            assertNull(recorded.getHeader(ProgressInterceptor.PROGRESS_TOKEN_HEADER))
        } finally {
            server.shutdown()
        }
    }
}
