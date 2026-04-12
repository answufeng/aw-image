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
        title = "Cache Management"

        val tvResult = findViewById<TextView>(R.id.tvResult)

        findViewById<Button>(R.id.btnClearMemory).setOnClickListener {
            val result = AwImage.clearMemoryCache(this)
            tvResult.text = "Memory cache cleared: $result"
        }

        findViewById<Button>(R.id.btnClearDisk).setOnClickListener {
            lifecycleScope.launch {
                val result = withContext(Dispatchers.IO) {
                    AwImage.clearDiskCache(this@CacheActivity)
                }
                tvResult.text = "Disk cache cleared: $result"
            }
        }
    }
}
