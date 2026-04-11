package com.answufeng.image.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.answufeng.image.AwImage
import com.answufeng.image.ImagePreloader
import com.answufeng.image.loadImage
import kotlinx.coroutines.launch

class PreloadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Preload"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val urls = listOf(
            "https://picsum.photos/200/200?random=1",
            "https://picsum.photos/200/200?random=2",
            "https://picsum.photos/200/200?random=3"
        )

        val tvStatus = TextView(this).apply { text = "Preloading..." }
        layout.addView(tvStatus)

        urls.forEach { url ->
            layout.addView(ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(200, 200)
                loadImage(url)
            })
        }

        lifecycleScope.launch {
            val success = ImagePreloader.preload(this@PreloadActivity, urls.first())
            tvStatus.text = "Preload result: $success"

            ImagePreloader.preloadAll(this@PreloadActivity, urls)
            tvStatus.append("\nBatch preload complete!")
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
