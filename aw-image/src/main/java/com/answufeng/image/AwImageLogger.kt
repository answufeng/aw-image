package com.answufeng.image

import android.util.Log

/**
 * aw-image 日志工具；默认关闭，通过 [AwImage.ImageConfig.enableLogging] 开启。
 *
 * release 中应保持关闭以免噪声与信息泄露（如 URL）。
 */
object AwImageLogger {

    /** 是否启用日志输出 */
    @Volatile
    var enabled: Boolean = false

    /** 日志 Tag（[AwImage.init] 时会先 [resetForInit] 再应用 [com.answufeng.image.AwImage.ImageConfig.logTag] / [com.answufeng.image.AwImage.ImageConfig.enableLogging]） */
    @Volatile
    private var tagState: String = "aw-image"
    var tag: String
        get() = tagState
        private set(value) {
            tagState = value
        }

    /** 与 [com.answufeng.image.AwImage.ImageConfig.logTag] 使用 */
    @JvmName("setTagValue")
    fun setTag(name: String) {
        val n = name.trim()
        if (n.isNotEmpty()) tag = n
    }

    /** [AwImage.init] 开头调用：关闭日志、恢复默认 tag */
    fun resetForInit() {
        enabled = false
        tag = "aw-image"
    }

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
