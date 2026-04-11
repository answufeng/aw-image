package com.answufeng.image.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.loadBlur
import com.answufeng.image.loadCircle
import com.answufeng.image.loadImage
import com.answufeng.image.loadRounded

class BasicLoadActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Basic Load"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val sampleUrl = "https://picsum.photos/400/300"
        val avatarUrl = "https://i.pravatar.cc/200"

        layout.addView(TextView(this).apply { text = "loadImage(url)" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(400, 300)
            loadImage(sampleUrl)
        })

        layout.addView(TextView(this).apply { text = "\nloadCircle(url)" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadCircle(avatarUrl)
        })

        layout.addView(TextView(this).apply { text = "\nloadRounded(url, 24f)" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(400, 300)
            loadRounded(sampleUrl, 24f)
        })

        layout.addView(TextView(this).apply { text = "\nloadBlur(url)" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(400, 300)
            loadBlur(sampleUrl)
        })

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
