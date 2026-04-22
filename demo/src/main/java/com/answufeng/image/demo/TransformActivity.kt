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

        val focus = intent.getStringExtra("focus")
        title = when (focus) {
            "blur" -> "模糊变换"
            "grayscale" -> "灰度变换"
            else -> "图片变换"
        }

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val url = "https://picsum.photos/400/300"

        when (focus) {
            "blur" -> setupBlurDemo(layout, url)
            "grayscale" -> setupGrayscaleDemo(layout, url)
            else -> setupFullDemo(layout, url)
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }

    private fun setupFullDemo(layout: LinearLayout, url: String) {
        layout.addView(createSectionLabel("原图"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply { topMargin = 12 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(url) { crossfade(false) }
        })

        layout.addView(createSectionLabel("圆角 (24px)"))
        layout.addView(createComparisonRow(url, isRounded = true))

        layout.addView(createSectionLabel("圆形"))
        layout.addView(createComparisonRow(url, isCircle = true))

        layout.addView(createSectionLabel("灰度"))
        layout.addView(createComparisonRow(url, isGrayscale = true))

        layout.addView(createSectionLabel("模糊"))
        layout.addView(createComparisonRow(url, isBlur = true))

        layout.addView(createSectionLabel("边框 (圆形 + 白色边框)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadCircleWithBorder(url, borderWidth = 4f, borderColor = Color.WHITE)
        })

        layout.addView(createSectionLabel("颜色滤镜"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadImage(url) {
                transform(ColorFilterTransformation(Color.parseColor("#80FF0000")))
            }
        })
    }

    private fun setupBlurDemo(layout: LinearLayout, url: String) {
        layout.addView(createSectionLabel("原图"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply { topMargin = 12 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadImage(url) { crossfade(false) }
        })

        layout.addView(createSectionLabel("模糊 (radius=10)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply { topMargin = 12 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadBlur(url, radius = 10)
        })

        layout.addView(createSectionLabel("模糊 (radius=20)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply { topMargin = 12 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadBlur(url, radius = 20)
        })

        layout.addView(createSectionLabel("模糊 (radius=25, sampling=2)"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300).apply { topMargin = 12 }
            scaleType = ImageView.ScaleType.CENTER_CROP
            loadBlur(url, radius = 25, sampling = 2)
        })
    }

    private fun setupGrayscaleDemo(layout: LinearLayout, url: String) {
        layout.addView(createSectionLabel("原图"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadImage(url) { crossfade(false) }
        })

        layout.addView(createSectionLabel("灰度"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadImage(url) { transform(GrayscaleTransformation()) }
        })

        layout.addView(createSectionLabel("灰度 + 圆角"))
        layout.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply { topMargin = 12 }
            loadImage(url) {
                roundedCorners(16f)
                transform(GrayscaleTransformation())
            }
        })
    }

    private fun createComparisonRow(
        url: String,
        isRounded: Boolean = false,
        isCircle: Boolean = false,
        isGrayscale: Boolean = false,
        isBlur: Boolean = false
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 }

            addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 200).apply {
                    weight = 1f
                    marginEnd = 8
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                loadImage(url) { crossfade(false) }
            })

            addView(ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 200).apply {
                    weight = 1f
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                when {
                    isRounded -> loadRounded(url, 24f)
                    isCircle -> loadCircle(url)
                    isGrayscale -> loadImage(url) { transform(GrayscaleTransformation()) }
                    isBlur -> loadBlur(url)
                }
            })
        }
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
