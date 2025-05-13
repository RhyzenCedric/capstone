package com.example.phishingapp

import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.example.phishingapp.backend.Infographic
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

        // Check if the URL is an SVG
        if (item.image_url.endsWith(".svg", ignoreCase = true)) {
            loadSvgImage(holder.imageView, item.image_url)
        } else {
            // Load regular image using Glide
            Glide.with(holder.imageView.context)
                .load(item.image_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imageView)
        }
    }

    private fun loadSvgImage(imageView: ImageView, url: String) {
        // Use coroutines to load SVG in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()

                val inputStream: InputStream = connection.inputStream
                val svg = SVG.getFromInputStream(inputStream)
                val drawable = PictureDrawable(svg.renderToPicture())

                withContext(Dispatchers.Main) {
                    imageView.setImageDrawable(drawable)
                }

                inputStream.close()
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("InfographicAdapter", "Error loading SVG: ${e.message}", e)

                // Fallback to regular Glide if SVG loading fails
                withContext(Dispatchers.Main) {
                    Glide.with(imageView.context)
                        .load(url)
                        .into(imageView)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}