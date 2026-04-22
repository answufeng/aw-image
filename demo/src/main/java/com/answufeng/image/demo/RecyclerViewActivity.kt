package com.answufeng.image.demo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.image.AwImage
import com.answufeng.image.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecyclerViewActivity : AppCompatActivity() {

    private val urls = (1..30).map { "https://picsum.photos/300/300?random=$it" }
    private var useGrid = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "RecyclerView 演示"

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val buttonBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val recyclerView = RecyclerView(this).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            layoutManager = LinearLayoutManager(this@RecyclerViewActivity)
        }

        val btnToggle = Button(this).apply {
            text = "切换 Grid"
            setOnClickListener {
                useGrid = !useGrid
                text = if (useGrid) "切换 List" else "切换 Grid"
                recyclerView.layoutManager = if (useGrid) {
                    GridLayoutManager(this@RecyclerViewActivity, 3)
                } else {
                    LinearLayoutManager(this@RecyclerViewActivity)
                }
            }
        }

        val btnCancelTag = Button(this).apply {
            text = "取消所有加载"
            setOnClickListener {
                AwImage.cancelByTag("rv_item")
            }
        }

        val btnPreload: Button = Button(this).apply {
            text = "预加载全部"
            setOnClickListener {
                lifecycleScope.launch {
                    val results = com.answufeng.image.ImagePreloader.preloadAll(
                        context = this@RecyclerViewActivity,
                        urls = this@RecyclerViewActivity.urls as List<Any>,
                        concurrency = 6
                    )
                    val successCount = results.count { it }
                    text = "预加载完成 ($successCount/${urls.size})"
                }
            }
        }

        buttonBar.addView(btnToggle)
        buttonBar.addView(btnCancelTag)
        buttonBar.addView(btnPreload)
        layout.addView(buttonBar)

        val statusText = TextView(this).apply {
            text = "提示：点击「取消所有加载」可批量取消正在进行的请求（cancelByTag）"
            textSize = 12f
            setTextColor(Color.parseColor("#888888"))
            setPadding(0, 8, 0, 8)
        }
        layout.addView(statusText)

        layout.addView(recyclerView)

        setContentView(layout)
        recyclerView.adapter = ImageAdapter(urls)
    }

    inner class ImageAdapter(private val urls: List<String>) :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        private val ivItemId = View.generateViewId()
        private val tvIndexId = View.generateViewId()

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(ivItemId)
            val tvIndex: TextView = view.findViewById(tvIndexId)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
                addView(TextView(parent.context).apply {
                    id = tvIndexId
                    textSize = 14f
                    setPadding(0, 0, 12, 0)
                })
                addView(ImageView(parent.context).apply {
                    id = ivItemId
                    layoutParams = LinearLayout.LayoutParams(200, 200)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setBackgroundColor(Color.parseColor("#FFE0E0E0"))
                })
            }
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.tvIndex.text = "#${position + 1}"
            holder.imageView.setImageResource(0)
            holder.imageView.loadImage(urls[position]) {
                tag("rv_item")
                lifecycle(this@RecyclerViewActivity)
                onStart { holder.tvIndex.text = "#${position + 1} ⏳" }
                onSuccess { holder.tvIndex.text = "#${position + 1} ✅" }
                onError { holder.tvIndex.text = "#${position + 1} ❌" }
            }
        }

        override fun getItemCount() = urls.size

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.tvIndex.text = "#${holder.adapterPosition + 1}"
        }
    }
}
