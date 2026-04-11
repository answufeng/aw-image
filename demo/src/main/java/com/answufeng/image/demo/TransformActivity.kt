package com.answufeng.image.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.*

class TransformActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Transformations"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val url = "https://picsum.photos/300/300"

        layout.addView(TextView(this).apply { text = "Grayscale" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300)
            loadImage(url) { transform(GrayscaleTransformation()) }
        })

        layout.addView(TextView(this).apply { text = "\nColor Filter (Red)" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300)
            loadImage(url) { transform(ColorFilterTransformation(0x33FF0000.toInt())) }
        })

        layout.addView(TextView(this).apply { text = "\nBlur" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300)
            loadBlur(url, radius = 20, sampling = 2)
        })

        layout.addView(TextView(this).apply { text = "\nBorder (Rectangle)" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300)
            loadImage(url) { transform(BorderTransformation(6f, Color.RED)) }
        })

        layout.addView(TextView(this).apply { text = "\nCircle + Border" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(300, 300)
            loadImage(url) {
                circle()
                transform(BorderTransformation(4f, Color.WHITE, circle = true))
            }
        })

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
