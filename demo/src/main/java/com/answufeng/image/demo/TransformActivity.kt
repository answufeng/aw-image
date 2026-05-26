package com.answufeng.image.demo

import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.LinearLayout
import com.answufeng.image.ColorFilterTransformation
import com.answufeng.image.CropTransformation
import com.answufeng.image.GrayscaleTransformation
import com.answufeng.image.WatermarkTransformation

class TransformActivity : DemoScaffoldActivity() {

    private val url = "https://picsum.photos/seed/aw-transform/800/500"
    private val filterUrl = "https://picsum.photos/seed/aw-filter/800/600"

    override fun demoTitle(): String = "形状与变换"

    override fun LinearLayout.buildDemo() {
        addSection(
            title = "快捷变换",
            subtitle = "左右对照；网络失败回退 img_test",
        ) {
            addComparisonRow("圆角", url) { loadDemoRounded(url, 24f) }
            addComparisonRow("圆形", url, square = true) { loadDemoCircle(url) }
            addComparisonRow("灰度", url) {
                loadDemoUrl(url) { transform(GrayscaleTransformation()) }
            }
            addComparisonRow("模糊", url) { loadDemoBlur(url, radius = 15, sampling = 4) }
        }

        addSection(
            title = "边框与色滤",
            subtitle = "头像类常用效果",
        ) {
            addCaption("loadCircleWithBorder（1:1 槽位）")
            addSquareImageSlot {
                loadDemoCircleWithBorder(url, 4f, Color.WHITE)
            }
            addCaption("ColorFilterTransformation")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadDemoUrl(url) {
                    transform(ColorFilterTransformation(Color.parseColor("#66FF5722")))
                }
            }
        }

        addSection(
            title = "滤镜对比",
            subtitle = "同一 URL，不同 Transformation",
        ) {
            addCaption("原图")
            addImageSlot { loadDemoUrl(filterUrl) }
            addCaption("GrayscaleTransformation")
            addImageSlot {
                loadDemoUrl(filterUrl) { transform(GrayscaleTransformation()) }
            }
            addCaption("ColorFilterTransformation（怀旧）")
            addImageSlot {
                loadDemoUrl(filterUrl) { transform(ColorFilterTransformation(0x779E775C)) }
            }
            addCaption("BlurTransformation")
            addImageSlot {
                loadDemoBlur(filterUrl, radius = 15, sampling = 4)
            }
        }

        addSection(
            title = "裁切与水印",
            subtitle = "CropTransformation / WatermarkTransformation",
        ) {
            addCaption("CropTransformation（中心区域）")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadDemoUrl(url) {
                    transform(CropTransformation(x = 200, y = 100, width = 400, height = 300))
                }
            }
            val watermark = BitmapFactory.decodeResource(resources, R.drawable.img_test)
            addCaption("WatermarkTransformation")
            addImageSlot(R.dimen.demo_image_height_small) {
                loadDemoUrl(url) {
                    transform(WatermarkTransformation(watermark, x = 16, y = 16, alpha = 160))
                }
            }
        }
    }
}
