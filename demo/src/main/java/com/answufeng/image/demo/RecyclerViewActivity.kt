package com.answufeng.image.demo

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answufeng.image.loadImage

class RecyclerViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "RecyclerView"

        val urls = (1..20).map { "https://picsum.photos/200/200?random=$it" }

        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@RecyclerViewActivity)
            adapter = ImageAdapter(urls)
        }

        setContentView(recyclerView)
    }

    class ImageAdapter(private val urls: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        class ViewHolder(val imageView: ImageView, val textView: TextView) : RecyclerView.ViewHolder(
            android.widget.LinearLayout(imageView.context).apply {
                orientation = android.widget.LinearLayout.HORIZONTAL
                setPadding(16, 8, 16, 8)
                addView(imageView.apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(120, 120)
                })
                addView(textView.apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(16, 0, 0, 0)
                })
            }
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ImageView(parent.context),
                TextView(parent.context)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.imageView.loadImage(urls[position])
            holder.textView.text = "Image #${position + 1}"
        }

        override fun getItemCount() = urls.size
    }
}
