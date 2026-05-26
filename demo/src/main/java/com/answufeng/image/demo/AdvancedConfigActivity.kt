package com.answufeng.image.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import com.answufeng.image.loadImage
import com.answufeng.image.loadRounded
import com.answufeng.image.loadRoundedDp
import com.answufeng.image.loadSquare
import com.answufeng.image.loadWithAspectRatio

class AdvancedConfigActivity : DemoScaffoldActivity() {

    private val url = "https://picsum.photos/seed/aw-adv/400/400"

    override fun demoTitle(): String = "圆角与 DSL"

    override fun LinearLayout.buildDemo() {
        addSection(
            title = "圆角",
            subtitle = "loadRounded / loadRoundedDp / 分角圆角 DSL",
        ) {
            addCaption("loadRounded(url, 24f)")
            addImageSlot(R.dimen.demo_image_height_small) { loadRounded(url, 24f) }
            addCaption("loadRoundedDp(url, 12f)")
            addImageSlot(R.dimen.demo_image_height_small) { loadRoundedDp(url, 12f) }
            addCaption("roundedCorners 仅顶部")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(url) {
                    roundedCorners(topLeft = 24f, topRight = 24f, bottomRight = 0f, bottomLeft = 0f)
                }
            }
        }

        addSection(
            title = "DSL 进阶",
            subtitle = "占位、尺寸、进度、raw",
        ) {
            addCaption("placeholder + override")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(url) {
                    placeholder(ColorDrawable(Color.parseColor("#FFE2E8F0")))
                    override(120, 120)
                }
            }
            addCaption("loadSquare / loadWithAspectRatio")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadSquare(url, edgePx = 100) { roundedCorners(8f) }
            }
            addImageSlot(R.dimen.demo_image_height_small) {
                loadWithAspectRatio(url, 16, 9, maxEdgePx = 160) { roundedCorners(4f) }
            }
            val progressLabel = addStatusLine("onProgressOnMainThread: …")
            addCaption("onProgressOnMainThread（大图加载进度，主线程回调）")
            addImageSlot {
                loadDemoUrl("https://picsum.photos/seed/aw-big/1200/1200") {
                    onProgressOnMainThread { cur, total ->
                        progressLabel.text = "onProgressOnMainThread: $cur / $total"
                    }
                }
            }
            addCaption("raw { allowHardware }")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(url) {
                    override(120, 120)
                    raw { allowHardware(true) }
                }
            }
        }
    }
}
