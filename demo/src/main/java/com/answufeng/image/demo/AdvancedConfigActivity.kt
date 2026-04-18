package com.answufeng.image.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.answufeng.image.loadCircle
import com.answufeng.image.loadCircleWithBorder
import com.answufeng.image.loadImage
import com.answufeng.image.loadRounded
import com.answufeng.image.loadRoundedDp

class AdvancedConfigActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val focus = intent.getStringExtra("focus")
        title = when (focus) {
            "circle" -> "圆形显示"
            "rounded" -> "圆角显示"
            else -> "Advanced Config"
        }

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val url = "https://picsum.photos/200/200"

        when (focus) {
            "circle" -> setupCircleDemo(layout, url)
            "rounded" -> setupRoundedDemo(layout, url)
            else -> setupFullDemo(layout, url)
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun setupCircleDemo(layout: LinearLayout, url: String) {
        layout.addView(createSectionLabel("圆形裁切"))
        layout.addView(createDescLabel("loadCircle(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadCircle(url)
        })

        layout.addView(createSectionLabel("圆形 + 白色边框"))
        layout.addView(createDescLabel("loadCircleWithBorder(url, 4f, Color.WHITE)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadCircleWithBorder(url, borderWidth = 4f, borderColor = Color.WHITE)
        })

        layout.addView(createSectionLabel("圆形 + 红色边框"))
        layout.addView(createDescLabel("loadCircleWithBorder(url, 6f, Color.RED)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadCircleWithBorder(url, borderWidth = 6f, borderColor = Color.RED)
        })
    }

    private fun setupRoundedDemo(layout: LinearLayout, url: String) {
        layout.addView(createSectionLabel("圆角 (px)"))
        layout.addView(createDescLabel("loadRounded(url, 24f)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadRounded(url, 24f)
        })

        layout.addView(createSectionLabel("圆角 (dp)"))
        layout.addView(createDescLabel("loadRoundedDp(url, 12f)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadRoundedDp(url, 12f)
        })

        layout.addView(createSectionLabel("顶部圆角"))
        layout.addView(createDescLabel("roundedCorners(topLeft=24, topRight=24, bottomRight=0, bottomLeft=0)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadImage(url) {
                roundedCorners(topLeft = 24f, topRight = 24f, bottomRight = 0f, bottomLeft = 0f)
            }
        })
    }

    private fun setupFullDemo(layout: LinearLayout, url: String) {
        layout.addView(TextView(this).apply { text = "Drawable placeholder + listener:" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(url) {
                placeholder(ColorDrawable(Color.parseColor("#FFE0E0E0")))
                error(ColorDrawable(Color.parseColor("#FFFFCDD2")))
                crossfade(500)
                override(200, 200)
                listener(
                    onStart = { layout.addView(TextView(this@AdvancedConfigActivity).apply { text = "Loading started..." }) },
                    onSuccess = { layout.addView(TextView(this@AdvancedConfigActivity).apply { text = "Loading succeeded!" }) },
                    onError = { layout.addView(TextView(this@AdvancedConfigActivity).apply { text = "Loading failed!" }) }
                )
            }
        })

        layout.addView(TextView(this).apply { text = "\nDrawable fallback (null data):" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(null) {
                fallback(ColorDrawable(Color.parseColor("#FFBDBDBD")))
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

        layout.addView(TextView(this).apply { text = "\nCrossfade disabled:" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(url) { crossfade(false) }
        })

        layout.addView(TextView(this).apply { text = "\nMemory cache only:" })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200)
            loadImage(url) { memoryCacheOnly() }
        })
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
        }
    }
}
