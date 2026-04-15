package com.answufeng.image.demo

import android.graphics.Color
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
        title = "基本加载"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        val sampleUrl = "https://picsum.photos/400/300"
        val avatarUrl = "https://picsum.photos/200"

        // 普通加载
        layout.addView(createSectionLabel("普通加载"))
        layout.addView(createDescLabel("loadImage(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl)
        })

        // 圆形加载
        layout.addView(createSectionLabel("圆形加载"))
        layout.addView(createDescLabel("loadCircle(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                topMargin = 12
            }
            loadCircle(avatarUrl)
        })

        // 圆角加载
        layout.addView(createSectionLabel("圆角加载"))
        layout.addView(createDescLabel("loadRounded(url, 24f)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadRounded(sampleUrl, 24f)
        })

        // 模糊加载
        layout.addView(createSectionLabel("模糊加载"))
        layout.addView(createDescLabel("loadBlur(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadBlur(sampleUrl)
        })

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

    private fun createDescLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(Color.parseColor("#888888"))
            setPadding(0, 0, 0, 0)
        }
    }
}
