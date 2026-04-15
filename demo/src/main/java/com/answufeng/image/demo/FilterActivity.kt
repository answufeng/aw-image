package com.answufeng.image.demo

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.answufeng.image.AwImage
import com.answufeng.image.transform.GrayscaleTransformation
import com.answufeng.image.transform.ColorFilterTransformation
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

class FilterActivity : BaseImageActivity() {

    override fun getTitleText() = "🌈 滤镜效果"

    override fun setupContent(layout: LinearLayout) {
        addSectionTitle("滤镜效果演示")

        val imageCard = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
            )
            setPadding(16, 16, 16, 16)
            layout.addView(this)
        }

        val imageView = ShapeableImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(16, 16, 16, 16)
            imageCard.addView(this)
        }

        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 16, 0, 16)
            layout.addView(this)
        }

        MaterialButton(this).apply {
            text = "原图"
            setOnClickListener {
                loadOriginalImage(imageView)
            }
            btnRow.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        MaterialButton(this).apply {
            text = "灰度"
            setOnClickListener {
                loadGrayscaleImage(imageView)
            }
            btnRow.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        MaterialButton(this).apply {
            text = "怀旧"
            setOnClickListener {
                loadSepiaImage(imageView)
            }
            btnRow.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        addDivider()

        addSectionTitle("滤镜信息")

        val infoCard = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
            )
            layout.addView(this)
        }

        val infoText = TextView(this).apply {
            text = "点击按钮查看不同滤镜效果"
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
            setTextColor(getColor(R.color.log_text))
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(16, 16, 16, 16)
            background = getDrawable(R.drawable.bg_log)
            infoCard.addView(this)
        }
    }

    private fun loadOriginalImage(imageView: ShapeableImageView) {
        AwImage.load("https://picsum.photos/800/600")
            .into(imageView)
    }

    private fun loadGrayscaleImage(imageView: ShapeableImageView) {
        AwImage.load("https://picsum.photos/800/600")
            .transform(GrayscaleTransformation())
            .into(imageView)
    }

    private fun loadSepiaImage(imageView: ShapeableImageView) {
        AwImage.load("https://picsum.photos/800/600")
            .transform(ColorFilterTransformation(0x779E775C))
            .into(imageView)
    }
}
