package com.answufeng.image.demo

import android.widget.LinearLayout
import com.answufeng.image.loadImage

class GifActivity : DemoScaffoldActivity() {

    override fun demoTitle(): String = "GIF 动图"

    override fun LinearLayout.buildDemo() {
        addSection(
            title = "Coil GIF",
            subtitle = "AwImage.init 默认 enableGif(true)",
        ) {
            val gifs = listOf(
                "https://media.giphy.com/media/BemKqR9RDK4V2/giphy.gif" to "示例 1",
                "https://media.giphy.com/media/xT9IgzoKnwFNmISR8I/giphy.gif" to "示例 2",
            )
            gifs.forEach { (url, label) ->
                addCaption(label)
                addImageSlot(R.dimen.demo_image_height_medium) { loadImage(url) }
            }
        }
    }
}
