package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.phishingapp.backend.RetrofitClient
import com.example.phishingapp.backend.Threat
import com.example.phishingapp.backend.ThreatsAdapter
import com.example.phishingapp.utils.DateUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalyticsActivity : AppCompatActivity() {
    private val TAG = "AnalyticsActivity"

    private lateinit var lineChart: LineChart
    private lateinit var adapter: ThreatsAdapter
    private var threatsList = mutableListOf<Threat>()
    private lateinit var timeUnitSpinner: Spinner
    private var selectedTimeUnit = "Days" // Default to Days

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Initializing AnalyticsActivity")
        setContentView(R.layout.activity_analytics)

        lineChart = findViewById(R.id.lineChart)
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewThreats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list
        adapter = ThreatsAdapter(threatsList)
        recyclerView.adapter = adapter
        Log.d(TAG, "onCreate: RecyclerView setup complete")

        setupNavigationButtons()
        setupTimeUnitSpinner()

        val userId = intent.extras?.getInt("userId", -1)
        Log.d(TAG, "onCreate: User ID from intent: $userId")

        if (userId != null && userId != -1) {
            Log.d(TAG, "onCreate: Fetching user links for user ID: $userId")
            fetchUserLinks(userId)
        } else {
            Log.e(TAG, "onCreate: Invalid User ID received")
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTimeUnitSpinner() {
        Log.d(TAG, "setupTimeUnitSpinner: Setting up time unit spinner")
        timeUnitSpinner = findViewById(R.id.spinnerTimeUnit)
        val timeUnits = arrayOf("Days") // Only "Days" remains
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeUnits)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeUnitSpinner.adapter = adapter

        // Handle dropdown selection
        timeUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTimeUnit = timeUnits[position]
                Log.d(TAG, "onItemSelected: Time unit selected: $selectedTimeUnit")
                setupLineChart() // Refresh the chart when a new unit is selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected: No time unit selected")
            }
        }
    }

    private fun fetchUserLinks(userId: Int) {
        Log.d(TAG, "fetchUserLinks: Fetching data for user ID: $userId")
        RetrofitClient.instance.getUserLinks(userId).enqueue(object : Callback<List<Threat>> {
            override fun onResponse(call: Call<List<Threat>>, response: Response<List<Threat>>) {
                Log.d(TAG, "onResponse: Response Code: ${response.code()}")

                if (response.isSuccessful) {
                    threatsList.clear()
                    response.body()?.let {
                        Log.d(TAG, "onResponse: Received ${it.size} threats from server")
                        threatsList.addAll(it)

                        // Log first few threats for debugging
                        for (i in 0 until minOf(5, it.size)) {
                            Log.d(TAG, "Threat $i: ID=${it[i].id}, Date=${it[i].date}")
                        }

                        setupLineChart()
                        adapter = ThreatsAdapter(threatsList)
                        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewThreats).adapter = adapter
                        Log.d(TAG, "onResponse: RecyclerView updated with new data")
                    } ?: run {
                        Log.e(TAG, "onResponse: Response body is null")
                    }
                } else {
                    Log.e(TAG, "onResponse: Request failed with code ${response.code()}")
                    Log.e(TAG, "onResponse: Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Threat>>, t: Throwable) {
                Log.e(TAG, "onFailure: Error fetching data", t)
                Log.e(TAG, "onFailure: Error message: ${t.message}")
                Log.e(TAG, "onFailure: Stack trace: ${t.stackTraceToString()}")
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupLineChart() {
        Log.d(TAG, "setupLineChart: Setting up line chart with ${threatsList.size} threats")

        if (threatsList.isEmpty()) {
            Log.d(TAG, "setupLineChart: No threat data available")
            lineChart.setNoDataText("No threat data available")
            lineChart.invalidate()
            return
        }

        // Create a data structure to hold dates and their corresponding threat counts
        val threatsByDate = processThreatDates()
        Log.d(TAG, "setupLineChart: Processed dates, found ${threatsByDate.size} unique dates")

        if (threatsByDate.isEmpty()) {
            Log.e(TAG, "setupLineChart: Could not process date data")
            lineChart.setNoDataText("Could not process date data")
            lineChart.invalidate()
            return
        }

        // Log the processed dates and counts
        Log.d(TAG, "Processed threat dates:")
        threatsByDate.forEach { (date, count) ->
            Log.d(TAG, "  $date: $count threats")
        }

        // Create entries for the chart
        val entries = mutableListOf<Entry>()
        val dateLabels = mutableListOf<String>()

        // Add entries in sorted order
        threatsByDate.entries.forEachIndexed { index, (dateStr, count) ->
            entries.add(Entry(index.toFloat(), count.toFloat()))
            dateLabels.add(dateStr)
        }

        Log.d(TAG, "setupLineChart: Created ${entries.size} chart entries")

        val dataSet = LineDataSet(entries, "Threats Over Time").apply {
            color = resources.getColor(R.color.ic_launcher_background, theme)
            valueTextColor = resources.getColor(R.color.black, theme)
            lineWidth = 2f
            setCircleColor(resources.getColor(R.color.ic_launcher_background, theme))
            circleRadius = 4f
            setValueTextSize(12f)
        }

        lineChart.data = LineData(dataSet)

        // Configure X-axis
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = 45f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < dateLabels.size) dateLabels[index] else ""
                }
            }
            setLabelCount(dateLabels.size.coerceAtMost(7), false) // Limit labels to avoid overcrowding
            setAvoidFirstLastClipping(true)
        }

        // Configure chart appearance
        lineChart.apply {
            description.isEnabled = false
            extraBottomOffset = 15f // Add padding for rotated labels
            animateX(500)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            legend.textSize = 12f
            Log.d(TAG, "setupLineChart: Chart configuration complete")
            invalidate() // Force a redraw
        }
    }

    private fun processThreatDates(): Map<String, Int> {
        Log.d(TAG, "processThreatDates: Processing ${threatsList.size} threats")
        val dateMap = mutableMapOf<String, Int>()
        var successCount = 0
        var failCount = 0

        // Store dates and counts
        for (threat in threatsList) {
            Log.d(TAG, "processThreatDates: Processing threat with date: ${threat.date}")
            // Use our consolidated DateUtils class to handle date parsing
            val formattedDate = DateUtils.formatDateForChart(threat.date)

            if (formattedDate != "Unknown") {
                // Increment the count for this date
                successCount++
                dateMap[formattedDate] = dateMap.getOrDefault(formattedDate, 0) + 1
                Log.d(TAG, "processThreatDates: Parsed date successfully: ${threat.date} -> $formattedDate")
            } else {
                failCount++
                Log.e(TAG, "processThreatDates: Failed to parse date: ${threat.date}")
            }
        }

        Log.d(TAG, "processThreatDates: Successfully parsed $successCount dates, failed to parse $failCount dates")
        Log.d(TAG, "processThreatDates: Found ${dateMap.size} unique dates")

        // Return dates sorted chronologically
        val sortedMap = sortDateMap(dateMap)
        Log.d(TAG, "processThreatDates: Sorted date map. First date: ${sortedMap.keys.firstOrNull()}, Last date: ${sortedMap.keys.lastOrNull()}")
        return sortedMap
    }

    /**
     * Sort date map chronologically with proper handling of months
     */
    private fun sortDateMap(dateMap: Map<String, Int>): Map<String, Int> {
        Log.d(TAG, "sortDateMap: Sorting ${dateMap.size} dates")

        val sortedMap = dateMap.toSortedMap { date1, date2 ->
            try {
                // Extract month number for proper comparison
                val month1 = DateUtils.monthToNumber(date1.substring(0, 3))
                val month2 = DateUtils.monthToNumber(date2.substring(0, 3))

                Log.d(TAG, "sortDateMap: Comparing $date1 (month $month1) with $date2 (month $month2)")

                if (month1 != month2) {
                    // Sort by month
                    month1.compareTo(month2)
                } else {
                    // If same month, sort by day
                    val day1 = date1.substring(4).toInt()
                    val day2 = date2.substring(4).toInt()
                    Log.d(TAG, "sortDateMap: Same month, comparing day $day1 with day $day2")
                    day1.compareTo(day2)
                }
            } catch (e: Exception) {
                // Fallback to string comparison
                Log.e(TAG, "sortDateMap: Error sorting dates: $date1, $date2", e)
                date1.compareTo(date2)
            }
        }

        Log.d(TAG, "sortDateMap: Sorted date map. Result: ${sortedMap.keys.joinToString(", ")}")
        return sortedMap
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: Activity resumed")
        lineChart.post {
            Log.d(TAG, "onResume: Refreshing chart")
            setupLineChart()
        }
    }

    private fun setupNavigationButtons() {
        Log.d(TAG, "setupNavigationButtons: Setting up navigation buttons")
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            Log.d(TAG, "Return icon clicked, navigating to AccountActivity")
            val intent = Intent(this, AccountActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}