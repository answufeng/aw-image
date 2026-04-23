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
            enableSvg(true)
            placeholder(ColorDrawable(Color.parseColor("#FFE2E8F0")))
            error(ColorDrawable(Color.parseColor("#FFFEE2E2")))
            logTag("aw-image-demo")
            enableLogging(BuildConfig.DEBUG)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        AwImage.onApplicationTrimMemory(this, level)
    }
}
