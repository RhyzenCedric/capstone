package com.example.phishingapp

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class DateAxisFormatter(private val timestamps: List<Long>) : ValueFormatter() {
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    override fun getFormattedValue(value: Float): String {
        val actualTimestamp = value.toLong() * 86400000 // Convert days back to full timestamp
        return dateFormat.format(Date(actualTimestamp))
    }
}


