package com.example.phishingapp.backend

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phishingapp.R
import java.text.SimpleDateFormat
import java.util.Locale

class ThreatsAdapter(private val threats: List<Threat>) :
    RecyclerView.Adapter<ThreatsAdapter.ThreatViewHolder>() {

    class ThreatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val urlTextView: TextView = view.findViewById(R.id.textViewUrl)
        val dateTextView: TextView = view.findViewById(R.id.textViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThreatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_threat, parent, false)
        return ThreatViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ThreatViewHolder, position: Int) {
        val threat = threats[position]
        holder.urlTextView.text = threat.url
        holder.dateTextView.text = "Verified: ${formatDate(threat.date)}"
    }
    override fun getItemCount() = threats.size

    // Function to format date
    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC") // Ensure it's parsed as UTC

            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            outputFormat.timeZone = java.util.TimeZone.getTimeZone("Asia/Manila") // Convert to Manila time

            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: "Invalid Date"
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}
