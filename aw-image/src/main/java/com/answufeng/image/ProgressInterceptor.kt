package com.answufeng.image

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 图片加载进度拦截器。
 *
 * 通过 OkHttp Interceptor 监听网络图片的下载进度，
 * 为注册了进度 token 的请求实时推送 (currentBytes, totalBytes) 进度。
 *
 * 每个带 [onProgress] 的请求会生成唯一 token，经请求头 [PROGRESS_TOKEN_HEADER] 关联（发出前会移除该头，不会到达服务端）。
 * 同一 URL 并发多请求时互不串扰。
 *
 * 线程安全：内部使用 [ConcurrentHashMap] 存储回调映射，
 * `register`/`unregister` 可在任意线程调用。
 *
 * 内存管理：请求完成或取消后必须调用 [unregister]（传入与 [register] 相同的
 * `listener` 引用）清理回调，否则会导致 token -> 监听器列表堆积。`AwImageScope` 在
 * onSuccess/onError/onCancel 及 Lifecycle 销毁时自动清理。
 *
 * @see Interceptor
 */
internal object ProgressInterceptor : Interceptor {

    /**
     * 内部进度关联头；由 [AwImageScope.registerProgressIfNeeded] 写入并在 [intercept] 中剥离。
     */
    const val PROGRESS_TOKEN_HEADER = "X-AwImage-Progress-Token"

    private val listeners = ConcurrentHashMap<String, CopyOnWriteArrayList<(Long, Long) -> Unit>>()

    fun register(token: String, listener: (Long, Long) -> Unit) {
        listeners.computeIfAbsent(token) { CopyOnWriteArrayList() }.add(listener)
    }

    /**
     * Removes [listener] for [token] (identity match). If no listeners remain, drops the entry.
     * Safe to call with a [listener] that was never registered.
     */
    fun unregister(token: String, listener: (Long, Long) -> Unit) {
        val list = listeners[token] ?: return
        list.remove(listener)
        if (list.isEmpty()) {
            listeners.remove(token)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = request.header(PROGRESS_TOKEN_HEADER)
        val forwardRequest = if (token != null) {
            request.newBuilder().removeHeader(PROGRESS_TOKEN_HEADER).build()
        } else {
            request
        }
        val response = chain.proceed(forwardRequest)
        val callbackList = token?.let { listeners[it] }
        if (callbackList != null && callbackList.isNotEmpty()) {
            val body = response.body ?: return response
            val contentLength = body.contentLength()
            val notify: (Long, Long) -> Unit = { c, t ->
                for (l in callbackList) {
                    l(c, t)
                }
            }
            val progressBody = object : ResponseBody() {
                private val progressSource = ProgressSource(body.source(), contentLength, notify)

                override fun contentType() = body.contentType()

                override fun contentLength() = contentLength

                override fun source(): BufferedSource = progressSource.buffer()
            }
            return response.newBuilder().body(progressBody).build()
        }
        return response
    }

    private class ProgressSource(
        delegate: Source,
        private val totalBytes: Long,
        private val notify: (Long, Long) -> Unit
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
            notify(current, total)
            return bytesRead
        }
    }
}
