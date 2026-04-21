package com.answufeng.image

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/**
 * 图片加载进度拦截器。
 *
 * 通过 OkHttp Interceptor 监听网络图片的下载进度，
 * 为注册了 URL 回调的请求实时推送 (currentBytes, totalBytes) 进度。
 *
 * 线程安全：内部使用 [ConcurrentHashMap] 存储回调映射，
 * `register`/`unregister` 可在任意线程调用。
 *
 * 内存管理：请求完成或取消后必须调用 [unregister] 清理回调，
 * 否则会导致 URL -> Listener 映射泄漏。`AwImageScope` 在
 * onSuccess/onError 时自动清理。
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
