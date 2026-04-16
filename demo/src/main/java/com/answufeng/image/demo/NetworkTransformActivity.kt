package com.answufeng.image.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.BlurTransformation
import com.answufeng.image.GrayscaleTransformation
import com.answufeng.image.loadImage

class NetworkTransformActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "网络变换"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        val sampleUrl = "https://picsum.photos/800/600"

        // 原图
        layout.addView(createSectionLabel("网络原图"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl)
        })

        // 灰度变换
        layout.addView(createSectionLabel("灰度变换"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl) {
                transform(GrayscaleTransformation())
            }
        })

        // 模糊变换
        layout.addView(createSectionLabel("模糊变换"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl) {
                transform(BlurTransformation(10, 2))
            }
        })

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun createSectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(android.graphics.Color.parseColor("#333333"))
            setPadding(0, 28, 0, 4)
        }
    }
}
