package com.answufeng.image.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "aw-image Demo"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val buttons = listOf(
            "Basic Load" to BasicLoadActivity::class.java,
            "Transformations" to TransformActivity::class.java,
            "Advanced Config" to AdvancedConfigActivity::class.java,
            "Preload" to PreloadActivity::class.java,
            "GIF" to GifActivity::class.java,
            "Cache Management" to CacheActivity::class.java,
            "RecyclerView" to RecyclerViewActivity::class.java
        )

        buttons.forEach { (label, clazz) ->
            Button(this).apply {
                text = label
                setAllCaps(false)
                textSize = 16f
                setPadding(0, 24, 0, 24)
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, clazz))
                }
                layout.addView(this)
            }
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
