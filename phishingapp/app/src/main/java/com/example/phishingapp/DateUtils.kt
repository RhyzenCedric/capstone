package com.example.phishingapp.utils

import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility class for date operations across the application
 * Centralizes date parsing/formatting to ensure consistency
 */
object DateUtils {
    private const val TAG = "DateUtils"

    // Input formats we might receive from the backend
    private val INPUT_FORMATS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd"
    )

    // Output formats for different use cases
    private const val CHART_FORMAT = "MMM-dd"
    private const val LIST_FORMAT = "MMM dd, yyyy HH:mm" // 24-hour format with minutes

    /**
     * Parse a date string into a Date object
     * @param dateString The date string to parse
     * @return The parsed Date or null if parsing failed
     */
    fun parseDate(dateString: String): Date? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
        )

        return formats.firstNotNullOfOrNull { format ->
            try {
                SimpleDateFormat(format, Locale.US).apply {
                    timeZone = if (format.endsWith("Z"))
                        TimeZone.getTimeZone("UTC")
                    else
                        TimeZone.getTimeZone("Asia/Manila")
                }.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Format a date for the analytics chart
     * @param dateString The date string to format
     * @return Formatted date string or "Unknown" if parsing failed
     */
    fun formatDateForChart(dateString: String): String {
        val date = parseDate(dateString)
        return if (date != null) {
            val formatter = SimpleDateFormat(CHART_FORMAT, Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC") // Keep UTC for chart
            formatter.format(date)
        } else {
            "Unknown"
        }
    }

    /**
     * Format a date for the threats list
     * @param dateString The date string to format
     * @return Formatted date string or "Invalid Date" if parsing failed
     */
    fun formatDateForList(dateString: String): String {
        return parseDate(dateString)?.let { date ->
            SimpleDateFormat(LIST_FORMAT, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Manila")
            }.format(date)
        } ?: "Invalid Date"
    }

    /**
     * Convert month abbreviation to number (1-12)
     */
    fun monthToNumber(monthAbbr: String): Int {
        val result = when (monthAbbr) {
            "Jan" -> 1
            "Feb" -> 2
            "Mar" -> 3
            "Apr" -> 4
            "May" -> 5
            "Jun" -> 6
            "Jul" -> 7
            "Aug" -> 8
            "Sep" -> 9
            "Oct" -> 10
            "Nov" -> 11
            "Dec" -> 12
            else -> 0
        }
        Log.d(TAG, "monthToNumber: Converted $monthAbbr to $result")
        return result
    }
}