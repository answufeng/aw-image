package com.answufeng.image.demo

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.AwImage

class CacheActivity : AppCompatActivity() {

    private val tv by lazy { TextView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Cache Management"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        layout.addView(Button(this).apply {
            text = "Clear Memory Cache"
            setOnClickListener {
                AwImage.clearMemoryCache(this@CacheActivity)
                tv.text = "Memory cache cleared!"
            }
        })

        layout.addView(Button(this).apply {
            text = "Clear Disk Cache"
            setOnClickListener {
                Thread {
                    AwImage.clearDiskCache(this@CacheActivity)
                    runOnUiThread { tv.text = "Disk cache cleared!" }
                }.start()
            }
        })

        layout.addView(tv)
        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
