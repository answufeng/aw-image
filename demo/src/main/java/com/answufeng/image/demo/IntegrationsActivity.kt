package com.answufeng.image.demo

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.answufeng.image.AwImage
import com.answufeng.image.ImagePreloader
import com.answufeng.image.loadImage
import kotlinx.coroutines.launch

/**
 * 集中演示：进度、tag、isCached、Lifecycle、SVG、memoryCacheOnly、请求头、离线说明等。
 */
class IntegrationsActivity : AppCompatActivity() {

    private val sampleImageUrl = "https://picsum.photos/400/300"
    private val tagKey = "demo_integrations_tag"
    private val svgUrl = "https://upload.wikimedia.org/wikipedia/commons/0/02/SVG_logo.svg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "高级集成"

        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        root.addView(sectionTitle("1. 下载进度 (onProgress)"))
        val tvProgress = TextView(this).apply {
            text = "Progress: (waiting)"
        }
        root.addView(tvProgress)
        val ivProgress = createIv()
        root.addView(ivProgress)
        ivProgress.loadImage(sampleImageUrl) {
            onProgress { cur, total ->
                val t = if (total >= 0) "$cur / $total" else "$cur (unknown total)"
                tvProgress.text = "Progress: $t"
            }
        }

        root.addView(sectionTitle("2. 标签与批量取消 (tag + cancelByTag)"))
        root.addView(
            TextView(this).apply {
                text = "先加载到下方；再点按钮取消 $tagKey 上全部请求"
                textSize = 12f
            }
        )
        val ivTag = createIv()
        root.addView(ivTag)
        ivTag.loadImage(sampleImageUrl) { tag(tagKey) }
        val btnCancelTag = com.google.android.material.button.MaterialButton(this).apply {
            text = "AwImage.cancelByTag(\"$tagKey\")"
            setOnClickListener { AwImage.cancelByTag(tagKey) }
        }
        root.addView(btnCancelTag)

        root.addView(sectionTitle("3. 缓存检查 (isCached)"))
        val tvCache = TextView(this).apply { text = "isCached: (tap button)"; textSize = 12f }
        root.addView(tvCache)
        val btnIsCached = com.google.android.material.button.MaterialButton(this).apply {
            text = "预加载后检查 isCached"
            setOnClickListener {
                lifecycleScope.launch {
                    val ok = ImagePreloader.preload(this@IntegrationsActivity, sampleImageUrl) {
                        size(400, 300)
                    }
                    val hit = AwImage.isCached(
                        this@IntegrationsActivity,
                        sampleImageUrl
                    ) {
                        size(400, 300)
                    }
                    tvCache.text = "preload=$ok, isCached(size 400x300)=$hit"
                }
            }
        }
        root.addView(btnIsCached)

        root.addView(sectionTitle("4. Lifecycle 绑定 (lifecycle)"))
        root.addView(
            TextView(this).apply {
                text = "页面销毁时自动取消；旋转屏幕可观察请求不泄漏到旧 Activity"
                textSize = 12f
            }
        )
        val ivLife = createIv()
        root.addView(ivLife)
        ivLife.loadImage(sampleImageUrl) {
            lifecycle(this@IntegrationsActivity)
        }

        root.addView(sectionTitle("5. SVG (enableSvg)"))
        root.addView(
            TextView(this).apply {
                text = "需在 AwImage.init { enableSvg(true) } 中开启（本 Demo 已开启）"
                textSize = 12f
            }
        )
        val ivSvg = createIv(180)
        root.addView(ivSvg)
        ivSvg.loadImage(svgUrl)

        root.addView(sectionTitle("6. 仅内存缓存 (memoryCacheOnly)"))
        root.addView(
            TextView(this).apply {
                text = "跳过磁盘与网络，仅读内存缓存；适合列表快速滑动（先预加载再试）"
                textSize = 12f
            }
        )
        val ivMemOnly = createIv(160)
        root.addView(ivMemOnly)
        ivMemOnly.loadImage(sampleImageUrl) {
            memoryCacheOnly()
            addHeader("Accept", "image/*")
        }

        root.addView(sectionTitle("7. 请求头 (addHeader)"))
        root.addView(
            TextView(this).apply {
                text = "上文已示例；亦可用 raw { } 写任意 ImageRequest.Builder API"
                textSize = 12f
            }
        )

        root.addView(sectionTitle("8. 离线策略 (offlineCacheEnabled)"))
        root.addView(
            TextView(this).apply {
                text = "默认 true：无网时仅走缓存。设为 false 可在断网时仍尝试网络（通常失败）。"
                textSize = 12f
            }
        )

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 14f
        setPadding(0, 16, 0, 8)
    }

    private fun createIv(heightDp: Int = 200): ImageView = ImageView(this).apply {
        val h = (heightDp * resources.displayMetrics.density).toInt()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            h
        ).apply { topMargin = 8; bottomMargin = 8 }
        scaleType = ImageView.ScaleType.CENTER_CROP
    }
}
