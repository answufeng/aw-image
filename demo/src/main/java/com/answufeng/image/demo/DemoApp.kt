package com.answufeng.image.demo

import android.app.Application
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.answufeng.image.AwImage

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AwImage.init(this) {
            memoryCacheSize(0.25)
            diskCacheSize(256L * 1024 * 1024)
            enableGif(true)
            placeholder(ColorDrawable(Color.parseColor("#FFE0E0E0")))
            error(ColorDrawable(Color.parseColor("#FFFFCDD2")))
            enableLogging(BuildConfig.DEBUG)
        }
    }
}
