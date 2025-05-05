package com.example.phishingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.phishingapp.backend.Infographic

class InfographicAdapter(private val items: List<Infographic>) :
    RecyclerView.Adapter<InfographicAdapter.InfographicViewHolder>() {

    class InfographicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.infoImage)
        val titleView: TextView = view.findViewById(R.id.infoTitle)
        val descView: TextView = view.findViewById(R.id.infoDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfographicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_infographic, parent, false)
        return InfographicViewHolder(view)
    }

    override fun onBindViewHolder(holder: InfographicViewHolder, position: Int) {
        val item = items[position]

        // Log the retrieved data
        Log.d("InfographicAdapter", "Item $position: title_text = ${item.title_text}, description = ${item.description}, image_url = ${item.image_url}")

        holder.titleView.text = item.title_text
        holder.descView.text = item.description

        // Load image using Glide or Picasso
        Glide.with(holder.imageView.context)
            .load(item.image_url)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = items.size
}
