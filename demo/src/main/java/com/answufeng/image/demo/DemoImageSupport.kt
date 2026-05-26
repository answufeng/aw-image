package com.answufeng.image.demo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.answufeng.image.AwImageScope
import com.answufeng.image.loadImage
import java.io.File
import java.io.FileOutputStream

/**
 * Demo 辅助：本地 Uri、网络失败时回退到 [R.drawable.img_test]。
 */
object DemoImageSupport {

    private const val CACHED_NAME = "demo_img_test.jpg"

    /**
     * 将内置 [R.drawable.img_test] 复制到应用 filesDir，返回 `file://` Uri（无需存储权限）。
     */
    fun localImageUri(context: Context): Uri {
        val file = File(context.filesDir, CACHED_NAME)
        if (!file.exists()) {
            val bitmap =
                BitmapFactory.decodeResource(context.resources, R.drawable.img_test)
                    ?: error("Failed to decode R.drawable.img_test")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        }
        return Uri.fromFile(file)
    }
}

/** 网络图加载失败时显示 Demo 内置本地图。 */
fun AwImageScope.demoUrlFallback() {
    error(R.drawable.img_test)
}

/** 带 [demoUrlFallback] 的 [loadImage] 快捷方式。 */
fun ImageView.loadDemoUrl(
    data: Any?,
    configure: AwImageScope.() -> Unit = {},
) {
    loadImage(data) {
        demoUrlFallback()
        configure()
    }
}

fun ImageView.loadDemoCircle(data: Any?) {
    loadImage(data) {
        demoUrlFallback()
        circle()
    }
}

fun ImageView.loadDemoRounded(
    data: Any?,
    radiusPx: Float,
) {
    loadImage(data) {
        demoUrlFallback()
        roundedCorners(radiusPx)
    }
}

fun ImageView.loadDemoBlur(
    data: Any?,
    radius: Int = 15,
    sampling: Int = 4,
) {
    loadImage(data) {
        demoUrlFallback()
        transform(com.answufeng.image.BlurTransformation(radius, sampling))
    }
}

fun ImageView.loadDemoCircleWithBorder(
    data: Any?,
    borderWidth: Float,
    borderColor: Int,
) {
    loadImage(data) {
        demoUrlFallback()
        transform(
            com.answufeng.image.BorderTransformation(borderWidth, borderColor, circle = true),
        )
    }
}
