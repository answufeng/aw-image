package com.answufeng.image.demo

import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.answufeng.image.AwImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CacheActivity : DemoScaffoldActivity() {

    override fun demoTitle(): String = "缓存管理"

    override fun LinearLayout.buildDemo() {
        val memLine = addStatusLine("内存: …")
        val diskLine = addStatusLine("磁盘: …")
        val actionLine = addStatusLine("操作结果将显示在此处")

        fun refresh() {
            memLine.text = "内存: ${formatSize(AwImage.getMemoryCacheSize(this@CacheActivity))}"
            lifecycleScope.launch {
                val disk = withContext(Dispatchers.IO) {
                    AwImage.getDiskCacheSize(this@CacheActivity)
                }
                diskLine.text = "磁盘: ${formatSize(disk)}"
            }
        }
        refresh()

        addSection(
            title = "清理缓存",
            subtitle = "AwImage.clearMemoryCache / clearDiskCache",
        ) {
            addPrimaryButton("清除内存缓存") {
                val ok = AwImage.clearMemoryCache(this@CacheActivity)
                actionLine.text = "clearMemoryCache: $ok"
                refresh()
            }
            addOutlinedButton("清除磁盘缓存") {
                lifecycleScope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        AwImage.clearDiskCache(this@CacheActivity)
                    }
                    actionLine.text = "clearDiskCache: $ok"
                    refresh()
                }
            }
            addOutlinedButton("全部清除") {
                lifecycleScope.launch {
                    val mem = AwImage.clearMemoryCache(this@CacheActivity)
                    val disk = withContext(Dispatchers.IO) {
                        AwImage.clearDiskCache(this@CacheActivity)
                    }
                    actionLine.text = "内存=$mem, 磁盘=$disk"
                    refresh()
                }
            }
        }

        addSection(
            title = "低内存",
            subtitle = "Application.onTrimMemory → AwImage.onApplicationTrimMemory",
        ) {
            addStatusLine(getString(R.string.cache_trim_memory_hint))
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val g = (kotlin.math.ln(bytes.toDouble()) / kotlin.math.ln(1024.0)).toInt()
            .coerceIn(0, units.lastIndex)
        return String.format("%.1f %s", bytes / Math.pow(1024.0, g.toDouble()), units[g])
    }
}
