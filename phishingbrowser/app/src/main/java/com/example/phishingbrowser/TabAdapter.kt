package com.example.phishingbrowser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TabAdapter(
    private val context: Context,
    private val tabTitles: List<String>,
    private val onTabSelected: (Int) -> Unit,
    private val onTabClosed: (Int) -> Unit,
    private val onNewTabClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TAB_TYPE = 1
    private val NEW_TAB_TYPE = 2

    override fun getItemViewType(position: Int): Int {
        return if (position == tabTitles.size) NEW_TAB_TYPE else TAB_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == NEW_TAB_TYPE) {
            val view = LayoutInflater.from(context).inflate(R.layout.new_tab_item, parent, false)
            NewTabViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.tab_item, parent, false)
            TabViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TabViewHolder) {
            val title = tabTitles[position]
            holder.tabTitle.text = title
            holder.itemView.setOnClickListener {
                onTabSelected(position)
            }
            holder.closeButton.setOnClickListener {
                onTabClosed(position)
            }
        } else if (holder is NewTabViewHolder) {
            holder.newTabButton.setOnClickListener {
                onNewTabClicked()
            }
        }
    }

    override fun getItemCount(): Int {
        return tabTitles.size + 1  // Includes the "New Tab" button
    }

    class TabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tabTitle: TextView = view.findViewById(R.id.tabTitle)
        val closeButton: Button = view.findViewById(R.id.closeButton)
    }

    class NewTabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val newTabButton: Button = view.findViewById(R.id.newTabButton)  // Reference to the Button in new_tab_item.xml
    }
}
