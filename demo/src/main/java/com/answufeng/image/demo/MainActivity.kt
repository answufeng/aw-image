package com.answufeng.image.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<MaterialButton>(R.id.btnLoadNetwork).setOnClickListener {
            startActivity(Intent(this, BasicLoadActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnLoadLocal).setOnClickListener {
            startActivity(Intent(this, BasicLoadActivity::class.java).apply {
                putExtra("source", "local")
            })
        }
        findViewById<MaterialButton>(R.id.btnLoadResource).setOnClickListener {
            startActivity(Intent(this, BasicLoadActivity::class.java).apply {
                putExtra("source", "resource")
            })
        }

        findViewById<MaterialButton>(R.id.btnRoundCorner).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAddFilter).setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnBlurImage).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java).apply {
                putExtra("focus", "blur")
            })
        }
        findViewById<MaterialButton>(R.id.btnGrayScale).setOnClickListener {
            startActivity(Intent(this, TransformActivity::class.java).apply {
                putExtra("focus", "grayscale")
            })
        }

        findViewById<MaterialButton>(R.id.btnShowCircle).setOnClickListener {
            startActivity(Intent(this, AdvancedConfigActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnShowRounded).setOnClickListener {
            startActivity(Intent(this, AdvancedConfigActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnShowPlaceholder).setOnClickListener {
            startActivity(Intent(this, ErrorHandlingActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnRecyclerView).setOnClickListener {
            startActivity(Intent(this, RecyclerViewActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnPreload).setOnClickListener {
            startActivity(Intent(this, PreloadActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnGif).setOnClickListener {
            startActivity(Intent(this, GifActivity::class.java))
        }

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
