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

        val source = intent.getStringExtra("source") ?: "network"
        title = when (source) {
            "local" -> "本地图片"
            "resource" -> "资源图片"
            else -> "网络图片"
        }

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
        }

        when (source) {
            "local" -> setupLocalImages(layout)
            "resource" -> setupResourceImages(layout)
            else -> setupNetworkImages(layout)
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun setupNetworkImages(layout: LinearLayout) {
        val sampleUrl = "https://picsum.photos/400/300"
        val avatarUrl = "https://picsum.photos/200"

        layout.addView(createSectionLabel("普通加载"))
        layout.addView(createDescLabel("loadImage(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(sampleUrl)
        })

        layout.addView(createSectionLabel("圆形加载"))
        layout.addView(createDescLabel("loadCircle(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                topMargin = 12
            }
            loadCircle(avatarUrl)
        })

        layout.addView(createSectionLabel("圆角加载"))
        layout.addView(createDescLabel("loadRounded(url, 24f)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadRounded(sampleUrl, 24f)
        })

        layout.addView(createSectionLabel("模糊加载"))
        layout.addView(createDescLabel("loadBlur(url)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadBlur(sampleUrl)
        })
    }

    private fun setupLocalImages(layout: LinearLayout) {
        layout.addView(createSectionLabel("本地文件加载"))
        layout.addView(createDescLabel("loadImage(File) — 将使用应用缓存目录下的测试文件"))
        layout.addView(TextView(this).apply {
            text = "提示：请将图片文件放入设备存储后使用文件路径加载"
            textSize = 13f
            setTextColor(Color.parseColor("#888888"))
            setPadding(0, 8, 0, 8)
        })
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply {
                topMargin = 12
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            val testFile = java.io.File(cacheDir, "test_image.jpg")
            if (testFile.exists()) {
                loadImage(testFile)
            } else {
                setImageResource(android.R.drawable.ic_menu_gallery)
            }
        })
    }

    private fun setupResourceImages(layout: LinearLayout) {
        layout.addView(createSectionLabel("资源图片加载"))
        layout.addView(createDescLabel("loadImage(R.drawable.xxx)"))

        layout.addView(createSectionLabel("系统图标"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                topMargin = 12
            }
            loadImage(android.R.drawable.ic_menu_gallery)
        })

        layout.addView(createSectionLabel("圆形资源图"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                topMargin = 12
            }
            loadCircle(android.R.drawable.ic_menu_gallery)
        })

        layout.addView(createSectionLabel("圆角资源图"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                topMargin = 12
            }
            loadRounded(android.R.drawable.ic_menu_gallery, 16f)
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
            setPadding(0, 0, 0, 0)
        }
    }
}
