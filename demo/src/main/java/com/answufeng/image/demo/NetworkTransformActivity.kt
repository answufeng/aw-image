package com.answufeng.image.demo

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.answufeng.image.AwImage
import com.answufeng.image.cache.AwImageCache
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView

class NetworkTransformActivity : BaseImageActivity() {

    override fun getTitleText() = "🔄 网络变换"

    override fun setupContent(layout: LinearLayout) {
        addSectionTitle("网络变换演示")

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
            text = "加载原图"
            setOnClickListener {
                loadOriginalImage(imageView)
            }
            btnRow.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        MaterialButton(this).apply {
            text = "加载压缩图"
            setOnClickListener {
                loadCompressedImage(imageView)
            }
            btnRow.addView(this, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        addDivider()

        addSectionTitle("变换信息")

        val infoCard = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200
            )
            layout.addView(this)
        }

        val infoText = TextView(this).apply {
            text = "点击按钮加载图片查看变换效果"
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
            setTextColor(getColor(R.color.log_text))
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(16, 16, 16, 16)
            background = getDrawable(R.drawable.bg_log)
            infoCard.addView(this)
        }

        // 监听图片加载状态
        AwImage.setOnImageLoadListener {
            infoText.text = "图片加载完成！"
        }
    }

    private fun loadOriginalImage(imageView: ShapeableImageView) {
        AwImage.load("https://picsum.photos/800/600")
            .into(imageView)
    }

    private fun loadCompressedImage(imageView: ShapeableImageView) {
        AwImage.load("https://picsum.photos/800/600")
            .resize(400, 300)
            .quality(70)
            .into(imageView)
    }
}
