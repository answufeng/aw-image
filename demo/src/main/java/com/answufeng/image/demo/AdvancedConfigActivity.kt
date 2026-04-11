package com.answufeng.image.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.loadImage

class AdvancedConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Advanced Config"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val url = "https://picsum.photos/200/200"

        layout.addView(TextView(this).apply { text = "Custom placeholder + error + listener:" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(url) {
                crossfade(500)
                override(200, 200)
                listener(
                    onStart = { layout.addView(TextView(this@AdvancedConfigActivity).apply { text = "Loading started..." }) },
                    onSuccess = { layout.addView(TextView(this@AdvancedConfigActivity).apply { text = "Loading succeeded!" }) },
                    onError = { layout.addView(TextView(this@AdvancedConfigActivity).apply { text = "Loading failed!" }) }
                )
            }
        })

        layout.addView(TextView(this).apply { text = "\nNo cache:" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(url) { noCache() }
        })

        layout.addView(TextView(this).apply { text = "\nOverride size (100x100):" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(url) { override(100, 100) }
        })

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
