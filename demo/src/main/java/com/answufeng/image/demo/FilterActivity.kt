package com.answufeng.image.demo

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.BlurTransformation
import com.answufeng.image.ColorFilterTransformation
import com.answufeng.image.GrayscaleTransformation
import com.answufeng.image.loadImage
import com.google.android.material.button.MaterialButton

class FilterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "滤镜效果"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        val sampleUrl = "https://picsum.photos/800/600"

        // 原图
        layout.addView(createSectionLabel("原图"))
        val originalIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl)
        }
        layout.addView(originalIv)

        // 灰度滤镜
        layout.addView(createSectionLabel("灰度滤镜"))
        val grayscaleIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl) {
                transform(GrayscaleTransformation())
            }
        }
        layout.addView(grayscaleIv)

        // 怀旧滤镜
        layout.addView(createSectionLabel("怀旧滤镜"))
        val sepiaIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl) {
                transform(ColorFilterTransformation(0x779E775C))
            }
        }
        layout.addView(sepiaIv)

        // 模糊滤镜
        layout.addView(createSectionLabel("模糊滤镜"))
        val blurIv = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl) {
                transform(BlurTransformation(15, 4))
            }
        }
        layout.addView(blurIv)

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun createSectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, 28, 0, 4)
        }
    }
}
