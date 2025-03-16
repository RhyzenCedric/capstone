package com.example.phishingapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var editTextDescription: EditText
    private lateinit var buttonSubmitReport: Button
    private var userId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        textViewUsername = findViewById(R.id.userUsername)
        editTextLink = findViewById(R.id.urlEditText)
        editTextDescription = findViewById(R.id.descriptionEditText)
        buttonSubmitReport = findViewById(R.id.reportButton)

        setupNavigationButtons()

        val username = intent.getStringExtra("userUsername") ?: "Guest"

        textViewUsername.text = username
        getUserIdFromUsername(username)

        val reportedLink = intent.getStringExtra("reportedLink")
        if (reportedLink != null) {
            editTextLink.setText(reportedLink) // Auto-fill the reported link field
        }

        // We try to get the userId from the intent
        val userId = intent.extras?.getInt("userId", -1)
        Log.d("ReportActivity", "Received username: $username, userId: $userId")

        if (userId == -1) {
            // if userId is -1, try fetching it via Retrofit
            getUserIdFromUsername(username)
        }

        buttonSubmitReport.setOnClickListener {
            if (userId != null) {
                submitReport(userId)
            }
        }
    }

    private fun getUserIdFromUsername(username: String) {
        if (username == "Guest") {
            Toast.makeText(this, "Guests cannot submit reports", Toast.LENGTH_SHORT).show()
            return
        }

        userId?.let {
            RetrofitClient.instance.getUserDetails(it).enqueue(object : Callback<UserDetails> {
                override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                    if (response.isSuccessful && response.body() != null) {
                        userId = response.body()?.userId
                        Log.d("ReportActivity", "User ID found: $userId")
                        Log.d("ReportActivitySuccess", "Response Body: ${response.body()}")
                        // Once we have the userId, we proceed with the report submission
                        submitReport(userId)
                    } else {
                        Toast.makeText(this@ReportActivity, "Failed to fetch user ID", Toast.LENGTH_SHORT).show()
                        Log.d("ReportActivityFail", "Response Body: ${response.body()}")
                    }
                }

                override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                    Log.e("ReportActivity", "Error fetching user ID: ${t.message}")
                    Toast.makeText(this@ReportActivity, "Failed to fetch user ID", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun submitReport(userId: Int?) {
        // Check if userId is still null or not yet retrieved
        Log.d("ReportActivitySubmit", "UserId: $userId")
        if (userId == null) {
            Toast.makeText(this, "Fetching user ID, please try again", Toast.LENGTH_SHORT).show()
            return
        }

        val link = editTextLink.text.toString().trim()
        val description = editTextDescription.text.toString().trim()

        if (link.isEmpty()) {
            Toast.makeText(this, "Please fill in the link you want to report", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Please add a report description", Toast.LENGTH_SHORT).show()
            return
        }

        val reportRequest = ReportRequest(userId!!, link, description)

        RetrofitClient.instance.submitReport(reportRequest).enqueue(object : Callback<ReportResponse> {
            override fun onResponse(call: Call<ReportResponse>, response: Response<ReportResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ReportActivity, "Report submitted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ReportActivity, "Failed to submit report", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ReportResponse>, t: Throwable) {
                Toast.makeText(this@ReportActivity, "Error submitting report: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun setupNavigationButtons() {
        // Get the username and userId from the current activity (ReportActivity)
        val username = intent.getStringExtra("userUsername") ?: "Guest"
        val userId = intent.extras?.getInt("userId", -1) // Retrieve userId from intent or set to -1 if not present

        findViewById<Button>(R.id.button_nav_home).setOnClickListener {
            // Only pass data if we are navigating to a different activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username and userId to MainActivity
            intent.putExtra("userUsername", username)
            intent.putExtra("userId", userId)

            startActivity(intent)
        }

        findViewById<Button>(R.id.button_nav_account).setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username and userId to AccountActivity
            intent.putExtra("userUsername", username)
            intent.putExtra("userId", userId)

            startActivity(intent)
        }
    }

}


