package com.example.phishingapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.phishingapp.backend.ReportRequest
import com.example.phishingapp.backend.ReportResponse
import com.example.phishingapp.backend.RetrofitClient
import com.example.phishingapp.backend.UserDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ReportActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var editTextLink: EditText
    private lateinit var buttonSubmitReport: ConstraintLayout
    private var userId: Int? = null
    private var editTextDescription: String = "" // Initialize with empty string

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        textViewUsername = findViewById(R.id.userUsername)
        editTextLink = findViewById(R.id.urlEditText)
        buttonSubmitReport = findViewById(R.id.reportButton)

        setupNavigationButtons()

        findViewById<Button>(R.id.buttonPhishing).setOnClickListener {
            selectDescription("Phishing")
        }

        findViewById<Button>(R.id.buttonSmishing).setOnClickListener {
            selectDescription("Smishing")
        }

        findViewById<Button>(R.id.buttonScam).setOnClickListener {
            selectDescription("Scam")
        }

        val username = intent.getStringExtra("userUsername") ?: "Guest"
        textViewUsername.text = username

        // We try to get the userId from the intent
        userId = intent.getIntExtra("userId", -1)
        if (userId == -1) {
            userId = null
            Log.d("ReportActivity", "Invalid userId received, will try to fetch it")
            getUserIdFromUsername(username)
        } else {
            Log.d("ReportActivity", "Received userId: $userId")
        }

        val reportedLink = intent.getStringExtra("reportedLink")
        if (reportedLink != null) {
            editTextLink.setText(reportedLink) // Auto-fill the reported link field
        }

        buttonSubmitReport.setOnClickListener {
            val currentUserId = userId
            if (currentUserId != null) {
                submitReport(currentUserId)
            } else {
                Toast.makeText(this, "User ID not available. Please try again.", Toast.LENGTH_SHORT).show()
                getUserIdFromUsername(username)
            }
        }
    }

    private fun selectDescription(description: String) {
        editTextDescription = description
        Toast.makeText(this, "Selected: $description", Toast.LENGTH_SHORT).show()

        // Disable all buttons after selection
        findViewById<Button>(R.id.buttonPhishing).isEnabled = false
        findViewById<Button>(R.id.buttonSmishing).isEnabled = false
        findViewById<Button>(R.id.buttonScam).isEnabled = false
    }

    private fun getUserIdFromUsername(username: String) {
        if (username == "Guest") {
            Toast.makeText(this, "Guests cannot submit reports", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun submitReport(userId: Int) {
        Log.d("ReportActivity", "Submitting report for userId: $userId")

        val link = editTextLink.text.toString().trim()

        if (link.isEmpty()) {
            Toast.makeText(this, "Please fill in the link you want to report", Toast.LENGTH_SHORT).show()
            return
        }

        if (editTextDescription.isEmpty()) {
            Toast.makeText(this, "Please select a report type", Toast.LENGTH_SHORT).show()
            return
        }

        // Create the report request with the proper field names to match the backend
        val reportRequest = ReportRequest(
            userId = userId,
            link_reported = link,          // Make sure this matches backend field name
            report_description = editTextDescription  // Make sure this matches backend field name
        )

        RetrofitClient.instance.submitReport(reportRequest).enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReportActivity, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                    Log.d("ReportActivity", "Report submitted successfully")

                    // Navigate back to MainActivity
                    val intent = Intent(this@ReportActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    intent.putExtra("userUsername", textViewUsername.text.toString())
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                    finish() // Close the ReportActivity to prevent going back to it
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("ReportActivity", "Error submitting report: $errorBody")
                        Toast.makeText(this@ReportActivity, "Failed to submit report: ${response.code()}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("ReportActivity", "Error parsing error response", e)
                        Toast.makeText(this@ReportActivity, "Failed to submit report", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Log.e("ReportActivity", "Network error submitting report", t)
                Toast.makeText(this@ReportActivity, "Error submitting report: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        LocalBroadcastManager.getInstance(this@ReportActivity)
            .sendBroadcast(Intent(ScreenCaptureService.ACTION_RESUME_SCANNING))
    }

    private fun setupNavigationButtons() {
        // Get the username and userId from the current activity (ReportActivity)
        val username = intent.getStringExtra("userUsername") ?: "Guest"
        val userId = intent.getIntExtra("userId", -1) // Retrieve userId from intent or set to -1 if not present

        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            // Only pass data if we are navigating to a different activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username and userId to MainActivity
            intent.putExtra("userUsername", username)
            intent.putExtra("userId", userId)

            startActivity(intent)
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ScreenCaptureService.ACTION_RESUME_SCANNING))
        }
    }
}