package com.answufeng.image.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.image.AwImage
import com.answufeng.image.ImagePreloader
import com.answufeng.image.loadSquare
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class RecyclerViewActivity : AppCompatActivity() {

    private val urls = (1..24).map { "https://picsum.photos/seed/rv$it/400/400" }
    private var gridMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_demo)

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val adapter = ImageAdapter(urls)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        findViewById<MaterialButton>(R.id.btnToggleLayout).setOnClickListener {
            gridMode = !gridMode
            recyclerView.layoutManager = if (gridMode) {
                GridLayoutManager(this, 2)
            } else {
                LinearLayoutManager(this)
            }
            (it as MaterialButton).text = getString(
                if (gridMode) R.string.demo_rv_toggle_list else R.string.demo_rv_toggle_grid,
            )
        }

        findViewById<MaterialButton>(R.id.btnCancelTag).setOnClickListener {
            AwImage.cancelByTag(TAG)
        }

        findViewById<MaterialButton>(R.id.btnPreloadAll).setOnClickListener { btn ->
            lifecycleScope.launch {
                (btn as MaterialButton).isEnabled = false
                val results = ImagePreloader.preloadAll(
                    context = this@RecyclerViewActivity,
                    urls = urls,
                    concurrency = 6,
                ) {
                    override(200, 200)
                }
                (btn as MaterialButton).text =
                    getString(R.string.demo_rv_preload_done, results.count { it }, urls.size)
                btn.isEnabled = true
            }
        }
    }

    private inner class ImageAdapter(private val urls: List<String>) :
        RecyclerView.Adapter<ImageAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val thumb: ImageView = view.findViewById(R.id.rowThumb)
            val index: TextView = view.findViewById(R.id.rowIndex)
            val status: TextView = view.findViewById(R.id.rowStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_demo_list_row, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.index.text = "#${position + 1}"
            holder.status.text = getString(R.string.demo_rv_loading)
            holder.thumb.setImageDrawable(null)
            holder.thumb.loadSquare(urls[position], edgePx = 200) {
                tag(TAG)
                lifecycle(this@RecyclerViewActivity)
                onSuccess { holder.status.text = getString(R.string.demo_rv_ok) }
                onError { holder.status.text = getString(R.string.demo_rv_fail) }
            }
        }

        override fun getItemCount(): Int = urls.size
    }

    companion object {
        private const val TAG = "demo_rv"
    }
}
