package com.answufeng.image.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.answufeng.image.loadImage
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

/**
 * Demo 统一 UI：脚手架 Toolbar + 分块卡片 + 图片槽位。
 */
object DemoUi

fun AppCompatActivity.setDemoScaffold(
    title: CharSequence,
    block: LinearLayout.() -> Unit,
) {
    setContentView(R.layout.activity_demo_scaffold)
    findViewById<MaterialToolbar>(R.id.toolbar).apply {
        setTitle(title)
        setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }
    findViewById<LinearLayout>(R.id.demoContent).block()
}

fun LinearLayout.addSection(
    title: CharSequence,
    subtitle: CharSequence,
    block: LinearLayout.() -> Unit,
) {
    val sectionRoot = LayoutInflater.from(context)
        .inflate(R.layout.include_demo_section, this, false)
    sectionRoot.findViewById<TextView>(R.id.sectionTitle).text = title
    sectionRoot.findViewById<TextView>(R.id.sectionSubtitle).text = subtitle
    sectionRoot.findViewById<LinearLayout>(R.id.sectionBody).block()
    addView(sectionRoot)
}

fun LinearLayout.addCaption(text: CharSequence) {
    addView(
        TextView(context).apply {
            this.text = text
            setTextAppearance(R.style.Text_AwImage_Caption)
            setPadding(0, 0, 0, 8)
        },
    )
}

fun LinearLayout.addImageSlot(
    @DimenRes heightRes: Int = R.dimen.demo_image_height_large,
    configure: ImageView.() -> Unit,
): ImageView {
    val imageView = LayoutInflater.from(context)
        .inflate(R.layout.include_demo_image, this, false) as ImageView
    val topMargin = (imageView.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    imageView.layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        resources.getDimensionPixelSize(heightRes),
    ).apply { this.topMargin = topMargin }
    imageView.configure()
    addView(imageView)
    return imageView
}

/** 正方形槽位，用于 loadCircle / loadCircleWithBorder 等需 1:1 容器的演示。 */
fun LinearLayout.addSquareImageSlot(
    @DimenRes sizeRes: Int = R.dimen.demo_image_square,
    configure: ImageView.() -> Unit,
): ImageView {
    val size = resources.getDimensionPixelSize(sizeRes)
    val imageView = LayoutInflater.from(context)
        .inflate(R.layout.include_demo_image, this, false) as ImageView
    val topMargin = (imageView.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    imageView.layoutParams = LinearLayout.LayoutParams(size, size).apply {
        this.topMargin = topMargin
    }
    imageView.configure()
    addView(imageView)
    return imageView
}

fun LinearLayout.addPrimaryButton(text: CharSequence, onClick: () -> Unit): MaterialButton {
    val button = MaterialButton(context, null, com.google.android.material.R.attr.materialButtonStyle).apply {
        this.text = text
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { topMargin = 8 }
        setOnClickListener { onClick() }
    }
    addView(button)
    return button
}

fun LinearLayout.addOutlinedButton(text: CharSequence, onClick: () -> Unit): MaterialButton {
    val button = LayoutInflater.from(context).inflate(
        R.layout.view_demo_outlined_button,
        this,
        false,
    ) as MaterialButton
    button.text = text
    button.setOnClickListener { onClick() }
    addView(button)
    return button
}

fun LinearLayout.addStatusLine(initial: CharSequence): TextView {
    val tv = TextView(context).apply {
        text = initial
        setTextAppearance(R.style.Text_AwImage_Body)
        setPadding(0, 4, 0, 8)
    }
    addView(tv)
    return tv
}

/**
 * 左右对照：左原图、右效果。圆形类效果请设 [square] = true，使两侧均为 1:1 槽位。
 */
fun LinearLayout.addComparisonRow(
    label: CharSequence,
    sourceUrl: String,
    square: Boolean = false,
    effect: ImageView.() -> Unit,
) {
    addCaption(label)
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }
    val gap = (6 * resources.displayMetrics.density).toInt()
    if (square) {
        val size = resources.getDimensionPixelSize(R.dimen.demo_image_square)
        row.addView(
            ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply { marginEnd = gap }
                scaleType = ImageView.ScaleType.CENTER_CROP
                loadDemoUrl(sourceUrl) { crossfade(false) }
            },
        )
        row.addView(
            ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply { marginStart = gap }
                scaleType = ImageView.ScaleType.CENTER_CROP
                effect()
            },
        )
    } else {
        val h = resources.getDimensionPixelSize(R.dimen.demo_image_height_medium)
        row.addView(
            ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, h, 1f).apply { marginEnd = gap }
                scaleType = ImageView.ScaleType.CENTER_CROP
                loadDemoUrl(sourceUrl) { crossfade(false) }
            },
        )
        row.addView(
            ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, h, 1f).apply { marginStart = gap }
                scaleType = ImageView.ScaleType.CENTER_CROP
                effect()
            },
        )
    }
    addView(row)
}
