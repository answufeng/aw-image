package com.answufeng.image.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DemoCatalogAdapter(
    private val items: List<DemoCatalogRow>,
    private val onEntryClick: (DemoCatalogRow.Entry) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is DemoCatalogRow.Header -> VIEW_HEADER
        is DemoCatalogRow.Entry -> VIEW_ENTRY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_HEADER -> HeaderHolder(
                inflater.inflate(R.layout.item_demo_catalog_header, parent, false),
            )
            else -> EntryHolder(
                inflater.inflate(R.layout.item_demo_catalog, parent, false),
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DemoCatalogRow.Header -> (holder as HeaderHolder).bind(item)
            is DemoCatalogRow.Entry -> (holder as EntryHolder).bind(item, onEntryClick)
        }
    }

    override fun getItemCount(): Int = items.size

    private class HeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.catalogHeader)
        fun bind(item: DemoCatalogRow.Header) {
            title.text = item.title
        }
    }

    private class EntryHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: TextView = view.findViewById(R.id.catalogIcon)
        private val entryTitle: TextView = view.findViewById(R.id.catalogTitle)
        private val subtitle: TextView = view.findViewById(R.id.catalogSubtitle)

        fun bind(item: DemoCatalogRow.Entry, onClick: (DemoCatalogRow.Entry) -> Unit) {
            icon.text = item.icon
            entryTitle.text = item.title
            subtitle.text = item.subtitle
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private const val VIEW_HEADER = 0
        private const val VIEW_ENTRY = 1
    }
}
