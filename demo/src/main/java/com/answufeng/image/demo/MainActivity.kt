package com.answufeng.image.demo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * aw-image 库功能演示
 * 包含：基本加载、变换、高级配置、错误处理、预加载、GIF、缓存管理、RecyclerView
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 图片加载
        findViewById<MaterialButton>(R.id.btnLoadNetwork).setOnClickListener {
            startActivity(Intent(this, BasicLoadActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnLoadLocal).setOnClickListener {
            startActivity(Intent(this, BasicLoadActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnLoadResource).setOnClickListener {
            startActivity(Intent(this, BasicLoadActivity::class.java))
        }

        // 图片处理
        findViewById<MaterialButton>(R.id.btnRoundCorner).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnCropImage).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnCompress).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAddFilter).setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnBlurImage).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnGrayScale).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java))
        }

        // 图片显示
        findViewById<MaterialButton>(R.id.btnShowCircle).setOnClickListener {
            startActivity(Intent(this, AdvancedConfigActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnShowRounded).setOnClickListener {
            startActivity(Intent(this, AdvancedConfigActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnShowPlaceholder).setOnClickListener {
            startActivity(Intent(this, ErrorHandlingActivity::class.java))
        }

        // 图片预览
        findViewById<MaterialButton>(R.id.btnPreviewZoom).setOnClickListener {
            startActivity(Intent(this, PreloadActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnPreviewRotate).setOnClickListener {
            startActivity(Intent(this, PreloadActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnPreviewGesture).setOnClickListener {
            startActivity(Intent(this, PreloadActivity::class.java))
        }

        // 图片缓存
        findViewById<MaterialButton>(R.id.btnMemoryCache).setOnClickListener {
            startActivity(Intent(this, CacheActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnDiskCache).setOnClickListener {
            startActivity(Intent(this, CacheActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnClearCache).setOnClickListener {
            startActivity(Intent(this, CacheActivity::class.java))
        }
    }
}
