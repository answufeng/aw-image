package com.answufeng.image.demo

import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.answufeng.image.AwImage
import com.answufeng.image.ImagePreloader
import com.answufeng.image.loadImage
import kotlinx.coroutines.launch

class IntegrationsActivity : DemoScaffoldActivity() {

    private val sampleUrl = "https://picsum.photos/seed/aw-int/600/400"
    private val tagKey = "demo_integrations_tag"
    private val svgUrl = "https://upload.wikimedia.org/wikipedia/commons/0/02/SVG_logo.svg"

    override fun demoTitle(): String = "高级能力"

    override fun LinearLayout.buildDemo() {
        addSection(
            title = "下载进度与取消",
            subtitle = "onProgress / onProgressOnMainThread；tag + cancelByTag",
        ) {
            val progressLine = addStatusLine("onProgress (子线程需 post): waiting…")
            addImageSlot(R.dimen.demo_image_height_medium) {
                loadDemoUrl(sampleUrl) {
                    onProgress { cur, total ->
                        val t = if (total >= 0) "$cur / $total" else "$cur (?)"
                        progressLine.post { progressLine.text = "onProgress: $t" }
                    }
                }
            }
            val progressMainLine = addStatusLine("onProgressOnMainThread: waiting…")
            addCaption("onProgressOnMainThread（直接更新 UI）")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadDemoUrl(sampleUrl) {
                    onProgressOnMainThread { cur, total ->
                        val t = if (total >= 0) "$cur / $total" else "$cur (?)"
                        progressMainLine.text = "onProgressOnMainThread: $t"
                    }
                }
            }
            addImageSlot(R.dimen.demo_image_height_small) {
                loadDemoUrl(sampleUrl) { tag(tagKey) }
            }
            addOutlinedButton("cancelByTag(\"$tagKey\")") {
                AwImage.cancelByTag(tagKey)
            }
        }

        addSection(
            title = "缓存与生命周期",
            subtitle = "isCached 须与 loadImage 同 override；lifecycle 自动取消",
        ) {
            val cacheLine = addStatusLine("isCached: 点击按钮检测")
            addOutlinedButton("预加载并 isCached") {
                lifecycleScope.launch {
                    val ok = ImagePreloader.preload(this@IntegrationsActivity, sampleUrl) {
                        override(400, 300)
                    }
                    val hit = AwImage.isCached(this@IntegrationsActivity, sampleUrl) {
                        override(400, 300)
                    }
                    cacheLine.text = "preload=$ok, isCached=$hit"
                }
            }
            addCaption("lifecycle(this) 绑定")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(sampleUrl) { lifecycle(this@IntegrationsActivity) }
            }
            addCaption("memoryCacheOnly()")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(sampleUrl) { memoryCacheOnly() }
            }
        }

        addSection(
            title = "SVG 与离线",
            subtitle = "DemoApp 已 enableSvg(true)；offlineCacheEnabled 默认 true",
        ) {
            addCaption("SVG")
            addImageSlot(R.dimen.demo_image_height_small) { loadImage(svgUrl) }
            addStatusLine("无网时 offlineCacheEnabled 仅走缓存，详见 README。")
        }
    }
}
