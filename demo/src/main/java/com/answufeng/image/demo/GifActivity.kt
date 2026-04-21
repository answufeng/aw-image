package com.answufeng.image.demo

import android.graphics.Color
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
        title = "GIF 动画"

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val gifUrls = listOf(
            "https://media.giphy.com/media/BemKqR9RDK4V2/giphy.gif" to "GIF 示例 1",
            "https://media.giphy.com/media/xT9IgzoKnwFNmISR8I/giphy.gif" to "GIF 示例 2",
            "https://media.giphy.com/media/l0HlvtIPdP7NpaLJK/giphy.gif" to "GIF 示例 3"
        )

        layout.addView(TextView(this).apply {
            text = "Coil 内置 GIF 解码支持，无需额外配置即可加载 GIF 动画。"
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            setPadding(0, 0, 0, 16)
        })

        gifUrls.forEach { (url, label) ->
            layout.addView(createSectionLabel(label))
            layout.addView(ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply {
                    topMargin = 12
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#FFE0E0E0"))
                loadImage(url)
            })
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun createSectionLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setTextColor(Color.parseColor("#333333"))
            setPadding(0, 24, 0, 4)
        }
    }
}
