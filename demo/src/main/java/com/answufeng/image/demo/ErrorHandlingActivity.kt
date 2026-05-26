package com.answufeng.image.demo

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.LinearLayout
import com.answufeng.image.loadCircleWithBorder
import com.answufeng.image.loadImage

class ErrorHandlingActivity : DemoScaffoldActivity() {

    private val validUrl = "https://picsum.photos/seed/aw-ok/300/300"
    private val invalidUrl = "https://invalid.example.com/not_found.jpg"

    override fun demoTitle(): String = "占位、错误与重试"

    override fun LinearLayout.buildDemo() {
        addSection(
            title = "加载状态",
            subtitle = "listener 监听 onStart / onSuccess / onError",
        ) {
            val validStatus = addStatusLine("有效 URL: …")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(validUrl) {
                    listener(
                        onStart = { validStatus.text = "有效 URL: 加载中" },
                        onSuccess = { validStatus.text = "有效 URL: 成功" },
                        onError = { validStatus.text = "有效 URL: 失败" },
                    )
                }
            }

            val errStatus = addStatusLine("无效 URL: …")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(invalidUrl) {
                    error(ColorDrawable(Color.parseColor("#FFFFCDD2")))
                    listener(
                        onError = { errStatus.text = "无效 URL: 已显示 error 图" },
                    )
                }
            }
        }

        addSection(
            title = "Fallback",
            subtitle = "data == null 时走 fallback 链",
        ) {
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(null) {
                    fallback(ColorDrawable(Color.parseColor("#FFBDBDBD")))
                }
            }
        }

        addSection(
            title = "重试",
            subtitle = "retry(2) + retryOnNetworkReconnect + lifecycle",
        ) {
            val retryStatus = addStatusLine("重试演示: …")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadImage(invalidUrl) {
                    retry(2)
                    retryOnNetworkReconnect()
                    lifecycle(this@ErrorHandlingActivity)
                    listener(
                        onError = { retryStatus.text = "重试演示: 失败，将自动重试" },
                        onSuccess = { retryStatus.text = "重试演示: 成功" },
                    )
                }
            }
            addCaption("圆形边框 + 错误图（1:1 槽位）")
            addSquareImageSlot {
                loadCircleWithBorder(invalidUrl, 4f, Color.RED)
            }
        }
    }
}
