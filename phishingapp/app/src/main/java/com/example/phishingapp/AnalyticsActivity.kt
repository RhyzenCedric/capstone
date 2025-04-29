package com.example.phishingapp

import android.annotation.SuppressLint
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.concurrent.TimeUnit

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var adapter: ThreatsAdapter
    private var threatsList = mutableListOf<Threat>()
    private lateinit var timeUnitSpinner: Spinner
    private var selectedTimeUnit = "Days" // Default to Days

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        lineChart = findViewById(R.id.lineChart)
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewThreats)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list to prevent the error
        adapter = ThreatsAdapter(threatsList)
        recyclerView.adapter = adapter

        setupNavigationButtons()

        // Setup the Spinner (Dropdown)
        timeUnitSpinner = findViewById(R.id.spinnerTimeUnit)
        val timeUnits = arrayOf("Days") // Only "Days" remains
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeUnits)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeUnitSpinner.adapter = adapter

        // Handle dropdown selection
        timeUnitSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTimeUnit = timeUnits[position] // Store selected time unit
                setupLineChart() // Refresh the chart when a new unit is selected
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val userId = intent.extras?.getInt("userId", -1)
        if (userId != null && userId != -1) {
            fetchUserLinks(userId)
        } else {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserLinks(userId: Int) {
        RetrofitClient.instance.getUserLinks(userId).enqueue(object : Callback<List<Threat>> {
            override fun onResponse(call: Call<List<Threat>>, response: Response<List<Threat>>) {
                Log.d("AnalyticsActivity", "Response Code: ${response.code()}")
                Log.d("AnalyticsActivity", "Response Body: ${response.errorBody()?.string()}")
                if (response.isSuccessful) {
                    threatsList.clear()
                    response.body()?.let {
                        threatsList.addAll(it)
                        setupLineChart()
                        adapter = ThreatsAdapter(threatsList)
                        findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewThreats).adapter = adapter
                    }
                } else {
                    Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Threat>>, t: Throwable) {
                Log.e("AnalyticsActivity", "Error fetching data: ${t.message}")
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupLineChart() {
        val entries = mutableListOf<Entry>()
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC") // Parse as UTC
        }
        val manilaFormat = SimpleDateFormat("MMM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Manila") // Convert to Manila time
        }

        val threatsByTime = mutableMapOf<String, Int>()

        for (threat in threatsList) {
            try {
                val utcDate = utcFormat.parse(threat.date)
                utcDate?.let {
                    // Convert to Manila time and format as MMM-dd
                    val manilaDateString = manilaFormat.format(it)

                    // Add or increment the count for this date
                    threatsByTime[manilaDateString] = threatsByTime.getOrDefault(manilaDateString, 0) + 1
                    Log.d("ThreatDate", "Original: ${threat.date}, Manila Date: $manilaDateString")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // We need to maintain an internal date format for sorting
        val sortFormat = SimpleDateFormat("MMM-dd", Locale.getDefault())

        // Sort the map by date
        val sortedEntries = threatsByTime.entries.sortedBy {
            try {
                sortFormat.parse(it.key)?.time ?: 0
            } catch (e: Exception) {
                0L
            }
        }
        Log.d("SortedEntries", sortedEntries.toString()) // Log sorted entries

        // Create a list for x-axis labels
        val xAxisLabels = sortedEntries.map { it.key }.toMutableList()

        // Create entries with index positions
        sortedEntries.forEachIndexed { index, (_, count) ->
            entries.add(Entry(index.toFloat(), count.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Threats Over Time").apply {
            color = resources.getColor(R.color.ic_launcher_background, theme)
            valueTextColor = resources.getColor(R.color.black, theme)
            lineWidth = 2f
            setCircleColor(resources.getColor(R.color.ic_launcher_background, theme))
            circleRadius = 4f
            setValueTextSize(12f)
            setValueTextColor(resources.getColor(R.color.black, theme))
        }

        lineChart.data = LineData(dataSet)
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            labelRotationAngle = 45f // Rotate labels 45 degrees
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < xAxisLabels.size) xAxisLabels[index] else ""
                }
            }
            // Add these lines:
            setLabelCount(xAxisLabels.size, true) // Force showing all labels
            setAvoidFirstLastClipping(true)
        }

// Also add this to ensure proper spacing
        lineChart.apply {
            description.isEnabled = false
            extraBottomOffset = 10f // Add padding at bottom for labels

            // Add these new lines:
            animateX(500) // Add animation to make the chart more visible
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDoubleTapToZoomEnabled(true)
            invalidate() // Force a redraw of the chart
        }
    }

    override fun onResume() {
        super.onResume()
        lineChart.post {
            setupLineChart()
        }
    }







    private fun setupNavigationButtons() {
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
