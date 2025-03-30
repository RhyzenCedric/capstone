package com.example.phishingapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var lineChart: LineChart
    private lateinit var adapter: ThreatsAdapter
    private var threatsList = mutableListOf<Threat>()

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
        val threatCounts = mutableMapOf<String, Int>()

        for (threat in threatsList) {
            val date = threat.date_verified.substring(0, 10)
            threatCounts[date] = threatCounts.getOrDefault(date, 0) + 1
        }

        var index = 0f
        for ((date, count) in threatCounts) {
            entries.add(Entry(index++, count.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Threats Over Time").apply {
            color = resources.getColor(R.color.ic_launcher_background, theme)
            valueTextColor = resources.getColor(R.color.black, theme)
            lineWidth = 2f
            setCircleColor(resources.getColor(R.color.ic_launcher_background, theme))
            circleRadius = 4f
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.setDrawGridLines(false)
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
