package com.answufeng.image

import android.content.Context
import android.content.ContextWrapper
import androidx.lifecycle.LifecycleOwner
import coil.request.ImageRequest
import okhttp3.OkHttpClient

/**
 * 从 [Context] 向上查找 [LifecycleOwner]（Activity / Fragment 等）。
 */
internal fun Context.findLifecycleOwner(): LifecycleOwner? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is LifecycleOwner) return current
        current = current.baseContext
    }
    return null
}

/**
 * 将 [AwImageScope] DSL 应用到 [ImageRequest.Builder]，与 [loadImage] 中配置语义一致。
 */
internal fun ImageRequest.Builder.applyAwImageScope(
    context: Context,
    data: Any?,
    config: (AwImageScope.() -> Unit)?,
) {
    if (config == null) return
    val scope = AwImageScope(this, data)
    scope.config()
    scope.applyTo(context)
}

internal fun OkHttpClient.ensureProgressInterceptor(): OkHttpClient {
    if (interceptors.any { it.javaClass == ProgressInterceptor::class.java }) {
        return this
    }
    return newBuilder().addInterceptor(ProgressInterceptor).build()
}
