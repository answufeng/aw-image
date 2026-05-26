package com.answufeng.image.demo

import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.answufeng.image.ImagePreloader
import com.answufeng.image.loadImage
import kotlinx.coroutines.launch

class PreloadActivity : DemoScaffoldActivity() {

    private val urls = listOf(
        "https://picsum.photos/seed/p1/400/400",
        "https://picsum.photos/seed/p2/400/400",
        "https://picsum.photos/seed/p3/400/400",
    )

    override fun demoTitle(): String = "预加载"

    override fun LinearLayout.buildDemo() {
        val status = addStatusLine("点击下方按钮开始预加载")

        addSection(
            title = "预加载后展示",
            subtitle = "预加载与 loadImage 使用相同 override(200,200)",
        ) {
            urls.forEachIndexed { index, url ->
                addCaption("图片 ${index + 1}")
                addImageSlot(R.dimen.demo_image_height_small) {
                    loadImage(url) { override(200, 200) }
                }
            }
            addPrimaryButton("单张预加载") {
                lifecycleScope.launch {
                    val ok = ImagePreloader.preload(this@PreloadActivity, urls.first()) {
                        override(200, 200)
                    }
                    status.text = "preload(首张): $ok"
                }
            }
            addOutlinedButton("批量预加载 (${urls.size} 张)") {
                lifecycleScope.launch {
                    val results = ImagePreloader.preloadAll(
                        this@PreloadActivity,
                        urls,
                        concurrency = 2,
                    ) {
                        override(200, 200)
                    }
                    status.text = "preloadAll: ${results.count { it }}/${urls.size} 成功"
                }
            }
        }
    }
}
