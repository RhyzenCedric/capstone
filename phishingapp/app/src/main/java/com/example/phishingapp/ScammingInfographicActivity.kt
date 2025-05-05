package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.phishingapp.backend.Infographic
import org.json.JSONArray
import com.android.volley.toolbox.JsonArrayRequest

class ScammingInfographicActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InfographicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scamminginfo_section)

        recyclerView = findViewById(R.id.infoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchRandomInfographics()

        setupNavigationButtons()
    }

    private fun fetchRandomInfographics() {
        // Replace with your API endpoint for fetching random infographics
        val url = "https://capstone-server-p18f.onrender.com/infographics?type=scam"

        val queue = Volley.newRequestQueue(this)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val infographics = mutableListOf<Infographic>()

                for (i in 0 until response.length()) {
                    val obj = response.getJSONObject(i)
                    val infographic = Infographic(
                        obj.getString("image_url"),
                        obj.getString("title_text"),
                        obj.getString("description")
                    )
                    infographics.add(infographic)
                }

                adapter = InfographicAdapter(infographics)
                recyclerView.adapter = adapter
            },
            { error ->
                // Log the error message
                Log.e("PhishingInfographicActivity", "Error fetching data: ${error.message}")
                Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonArrayRequest)
    }

    private fun setupNavigationButtons() {

        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            // Only pass data if we are navigating to a different activity
            val intent = Intent(this, LearnMoreMainScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ScreenCaptureService.ACTION_RESUME_SCANNING))
        }
    }
}