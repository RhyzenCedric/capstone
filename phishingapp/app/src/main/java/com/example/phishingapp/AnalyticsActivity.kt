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
        val manilaFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("Asia/Manila") // Convert to Manila time
        }

        val threatsByTime = mutableMapOf<Long, Int>()

        for (threat in threatsList) {
            try {
                val utcDate = utcFormat.parse(threat.date_verified)
                utcDate?.let {
                    val manilaDateString = manilaFormat.format(it) // Convert to Manila time string
                    val manilaDate = manilaFormat.parse(manilaDateString) // Convert back to Date object

                    // Normalize date to midnight to ensure correct day parsing
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"))
                    calendar.time = manilaDate
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    val timeInDays: Long = calendar.timeInMillis / (1000 * 60 * 60 * 24) // Convert to days
                    threatsByTime[timeInDays] = threatsByTime.getOrDefault(timeInDays, 0) + 1
                    Log.d("ThreatDate", "Original: ${threat.date_verified}, Manila Time: $manilaDateString, Parsed Days: $timeInDays")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val sortedEntries = threatsByTime.toSortedMap()
        Log.d("SortedEntries", sortedEntries.toString()) // Log sorted entries

        val xAxisValues: MutableList<Long> = sortedEntries.keys.toMutableList()

        for ((time, count) in sortedEntries) {
            entries.add(Entry(time.toFloat(), count.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Threats Over Time").apply {
            color = resources.getColor(R.color.ic_launcher_background, theme)
            valueTextColor = resources.getColor(R.color.black, theme)
            lineWidth = 2f
            setCircleColor(resources.getColor(R.color.ic_launcher_background, theme))
            circleRadius = 4f
        }

        lineChart.data = LineData(dataSet)
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f // Ensure only distinct dates are shown
            valueFormatter = DateAxisFormatter(xAxisValues)
        }

        lineChart.axisLeft.setDrawGridLines(false)
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.invalidate()
    }








    private fun setupNavigationButtons() {
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }
}
