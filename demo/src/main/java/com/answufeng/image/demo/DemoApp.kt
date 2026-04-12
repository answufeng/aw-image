package com.answufeng.image.demo

import android.app.Application
import com.answufeng.image.AwImage

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AwImage.init(this) {
            memoryCacheSize(0.25)
            diskCacheSize(256L * 1024 * 1024)
            enableGif(true)
            enableLogging(true)
        }
    }
}
