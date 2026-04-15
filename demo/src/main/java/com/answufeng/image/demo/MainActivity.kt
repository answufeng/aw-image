package com.answufeng.image.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

/**
 * aw-image 库功能演示
 * 包含：基本加载、变换、高级配置、错误处理、预加载、GIF、缓存管理、RecyclerView
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 主布局
        val mainLayout = findViewById<LinearLayout>(R.id.mainLayout)

        // 标题
        mainLayout.addView(TextView(this).apply {
            text = "🖼️ aw-image 功能演示"
            textSize = 20f
            setPadding(0, 0, 0, 20)
        })

        // 功能卡片
        val features = listOf(
            "📱 基本加载" to BasicLoadActivity::class.java,
            "🎨 图片变换" to TransformActivity::class.java,
            "⚙️ 高级配置" to AdvancedConfigActivity::class.java,
            "⚠️ 错误处理" to ErrorHandlingActivity::class.java,
            "⏳ 预加载" to PreloadActivity::class.java,
            "🎯 GIF 支持" to GifActivity::class.java,
            "💾 缓存管理" to CacheActivity::class.java,
            "📋 RecyclerView" to RecyclerViewActivity::class.java,
            "🔄 网络变换" to NetworkTransformActivity::class.java,
            "🌈 滤镜效果" to FilterActivity::class.java
        )

        features.forEach { (title, clazz) ->
            val card = MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                setPadding(20, 20, 20, 20)
            }

            val cardContent = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            cardContent.addView(TextView(this).apply {
                text = title
                textSize = 16f
                setPadding(0, 0, 0, 8)
            })

            cardContent.addView(Button(this).apply {
                text = "进入演示"
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, clazz))
                }
            })

            card.addView(cardContent)
            mainLayout.addView(card)
        }
    }
}
