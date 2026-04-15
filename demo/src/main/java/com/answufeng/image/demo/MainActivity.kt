package com.answufeng.image.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * aw-image 库功能演示
 * 包含：基本加载、变换、高级配置、错误处理、预加载、GIF、缓存管理、RecyclerView
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "AwImage Demo"

        // 主容器
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 30, 20, 20)
        }

        // 标题
        mainLayout.addView(TextView(this).apply {
            text = "🖼️ aw-image 功能演示"
            textSize = 20f
            setPadding(0, 0, 0, 20)
        })

        // 功能说明
        mainLayout.addView(TextView(this).apply {
            text = "功能列表："
            textSize = 16f
            setPadding(0, 10, 0, 10)
        })

        // 功能按钮
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val buttons = listOf(
            "📱 基本加载" to BasicLoadActivity::class.java,
            "🎨 图片变换" to TransformActivity::class.java,
            "⚙️ 高级配置" to AdvancedConfigActivity::class.java,
            "⚠️ 错误处理" to ErrorHandlingActivity::class.java,
            "⏳ 预加载" to PreloadActivity::class.java,
            "🎯 GIF 支持" to GifActivity::class.java,
            "💾 缓存管理" to CacheActivity::class.java,
            "📋 RecyclerView" to RecyclerViewActivity::class.java
        )

        buttons.forEach { (label, clazz) ->
            buttonLayout.addView(createButton(label) {
                startActivity(Intent(this@MainActivity, clazz))
            })
        }

        mainLayout.addView(buttonLayout)

        // 滚动视图
        val scrollView = ScrollView(this).apply {
            addView(mainLayout)
        }

        setContentView(scrollView)
    }

    private fun createButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            setOnClickListener { onClick() }
        }
    }
}