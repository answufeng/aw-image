package com.answufeng.image.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.loadImage

class GifActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "GIF Support"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        layout.addView(TextView(this).apply {
            text = "GIF images are loaded automatically.\nJust use loadImage() with a GIF URL."
        })

        val gifUrl = "https://media.giphy.com/media/JIX9t2j0ZTN9S/giphy.gif"

        layout.addView(TextView(this).apply { text = "\nGIF:" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(400, 400)
            loadImage(gifUrl)
        })

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
