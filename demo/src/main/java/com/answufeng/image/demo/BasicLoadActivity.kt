package com.answufeng.image.demo

import android.widget.LinearLayout
import com.answufeng.image.loadImage

class BasicLoadActivity : DemoScaffoldActivity() {

    private val sampleUrl = "https://picsum.photos/seed/aw-basic/800/500"
    private val avatarUrl = "https://picsum.photos/seed/aw-avatar/400/400"

    override fun demoTitle(): String = "数据源加载"

    override fun LinearLayout.buildDemo() {
        addSection(
            title = "网络 URL",
            subtitle = "失败时 error 回退 img_test；圆形须 1:1 槽位",
        ) {
            addCaption("loadImage(url)")
            addImageSlot { loadDemoUrl(sampleUrl) }
            addCaption("loadCircle(url) — 1:1 槽位")
            addSquareImageSlot { loadDemoCircle(avatarUrl) }
            addCaption("loadRounded(url, 24f)")
            addImageSlot { loadDemoRounded(sampleUrl, 24f) }
            addCaption("loadBlur(url)")
            addImageSlot { loadDemoBlur(sampleUrl, radius = 12, sampling = 4) }
        }

        addSection(
            title = "Drawable 资源",
            subtitle = "loadImage(R.drawable.img_test) 等，Demo 内置 res/drawable/img_test.jpg",
        ) {
            addCaption("loadImage(R.drawable.img_test)")
            addImageSlot(R.dimen.demo_image_height_medium) {
                loadImage(R.drawable.img_test)
            }
            addCaption("loadCircle(R.drawable.img_test) — 1:1 槽位")
            addSquareImageSlot {
                loadImage(R.drawable.img_test) { circle() }
            }
            addCaption("loadRounded(R.drawable.img_test, 16f)")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(R.drawable.img_test) { roundedCorners(16f) }
            }
        }

        addSection(
            title = "Uri（应用内文件）",
            subtitle = "由 drawable 复制到 filesDir，无需 READ 存储权限；相册 content:// 用法相同",
        ) {
            val uri = DemoImageSupport.localImageUri(context)
            addCaption("loadImage(contentUri)")
            addImageSlot(R.dimen.demo_image_height_medium) {
                loadImage(uri)
            }
        }
    }
}
