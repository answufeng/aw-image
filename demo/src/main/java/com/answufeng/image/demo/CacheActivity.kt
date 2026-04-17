package com.answufeng.image.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.answufeng.image.AwImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CacheActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cache)
        title = "缓存管理"

        val tvResult = findViewById<TextView>(R.id.tvResult)
        val tvMemSize = findViewById<TextView>(R.id.tvMemSize)
        val tvDiskSize = findViewById<TextView>(R.id.tvDiskSize)

        refreshCacheSizes(tvMemSize, tvDiskSize)

        findViewById<Button>(R.id.btnClearMemory).setOnClickListener {
            val result = AwImage.clearMemoryCache(this)
            tvResult.text = "内存缓存已清除: $result"
            refreshCacheSizes(tvMemSize, tvDiskSize)
        }

        findViewById<Button>(R.id.btnClearDisk).setOnClickListener {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    AwImage.clearDiskCache(this@CacheActivity)
                }
                tvResult.text = "磁盘缓存已清除: $result"
                refreshCacheSizes(tvMemSize, tvDiskSize)
            }
        }
    }

    private fun refreshCacheSizes(tvMemSize: TextView, tvDiskSize: TextView) {
        val memSize = AwImage.getMemoryCacheSize(this)
        tvMemSize.text = "内存缓存: ${formatSize(memSize)}"

        lifecycleScope.launch {
            val diskSize = withContext(Dispatchers.IO) {
                AwImage.getDiskCacheSize(this@CacheActivity)
            }
            tvDiskSize.text = "磁盘缓存: ${formatSize(diskSize)}"
        }
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            .coerceAtMost(units.size - 1)
        return String.format("%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
