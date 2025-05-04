package com.example.phishingapp.backend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phishingapp.R
import com.example.phishingapp.utils.DateUtils

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

    override fun onBindViewHolder(holder: ThreatViewHolder, position: Int) {
        val threat = threats[position]
        holder.urlTextView.text = threat.url
        holder.dateTextView.text = "Verified: ${DateUtils.formatDateForList(threat.date)}"
    }

    override fun getItemCount() = threats.size
}