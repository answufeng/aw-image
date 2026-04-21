package com.answufeng.image

import android.util.Log

/**
 * aw-image 内部日志工具。
 *
 * 默认关闭，通过 [AwImage.ImageConfig.enableLogging] 开启：
 * ```kotlin
 * AwImage.init(this) {
 *     enableLogging(BuildConfig.DEBUG)
 * }
 * ```
 */
internal object AwImageLogger {

    /** 是否启用日志输出 */
    @Volatile
    var enabled: Boolean = false

    /** 日志 Tag */
    var tag: String = "aw-image"
        private set

    fun d(message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (enabled) {
            if (throwable != null) Log.e(tag, message, throwable)
            else Log.e(tag, message)
        }
    }
}
