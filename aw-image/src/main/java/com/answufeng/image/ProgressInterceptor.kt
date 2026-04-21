package com.answufeng.image

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 图片加载进度拦截器
 *
 * 用于监听图片加载的进度，通过 OkHttp Interceptor 实现
 *
 * @see Interceptor
 */
internal object ProgressInterceptor : Interceptor {

    private val listeners = ConcurrentHashMap<String, (Long, Long) -> Unit>()

    fun register(url: String, listener: (Long, Long) -> Unit) {
        listeners[url] = listener
    }

    fun unregister(url: String) {
        listeners.remove(url)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val url = request.url.toString()
        val listener = listeners[url]
        if (listener != null) {
            val body = response.body ?: return response
            val contentLength = body.contentLength()
            val source = ProgressSource(body.source(), contentLength, listener)
            val newBody = body.newBuilder()
                .source(source.buffer())
                .build()
            return response.newBuilder().body(newBody).build()
        }
        return response
    }

    private class ProgressSource(
        delegate: Source,
        private val totalBytes: Long,
        private val listener: (Long, Long) -> Unit
    ) : ForwardingSource(delegate) {

        private var bytesRead = 0L

        @Throws(IOException::class)
        override fun read(sink: Buffer, byteCount: Long): Long {
            val bytesRead = super.read(sink, byteCount)
            if (bytesRead != -1L) {
                this.bytesRead += bytesRead
            }
            val current = this.bytesRead
            val total = if (totalBytes != -1L) totalBytes else -1L
            listener(current, total)
            return bytesRead
        }
    }
}
