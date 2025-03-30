package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.phishingapp.MainActivity.Companion.TAG

class AccountActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var buttonLogout: ConstraintLayout
    private lateinit var buttonAnalytics: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        setupNavigationButtons()
        // Get username passed from MainActivity
        val username = intent.getStringExtra("userUsername") ?: "Guest"
        val userId = intent.extras?.getInt("userId", -1)

        // Log the username
        Log.d("AccountActivity", "Received username: $username")
        Log.d("AccountActivity", "User ID: $userId")

        // Initialize UI elements
        textViewUsername = findViewById(R.id.textViewUsername)
        buttonLogout = findViewById(R.id.logout_button_plate)
        buttonAnalytics =findViewById(R.id.analytics_plate)

        // Set the username
        textViewUsername.text = username

        // Handle logout
        buttonLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

    }

    private fun setupNavigationButtons() {
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username to MainActivity
            val username = intent.getStringExtra("userUsername") ?: "Guest"
            intent.putExtra("userUsername", username)

            startActivity(intent)
        }

        findViewById<ConstraintLayout>(R.id.analytics_plate).setOnClickListener {
            if (javaClass != AnalyticsActivity::class.java) {
                val username = intent.getStringExtra("userUsername") ?: "Guest"
                val userId = intent.extras?.getInt("userId")
                Log.d(TAG, "Passing userId: $userId")
                val intent = Intent(this@AccountActivity, AnalyticsActivity::class.java)
                intent.putExtra("userUsername", username)
                intent.putExtra("userId", userId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already on Analytics Screen", Toast.LENGTH_SHORT).show()
            }
        }

        /*findViewById<Button>(R.id.button_nav_account).setOnClickListener {
            if (javaClass != AccountActivity::class.java) {
                val username = intent.getStringExtra("userUsername") ?: "Guest"
                val intent = Intent(this@AccountActivity, AccountActivity::class.java)
                intent.putExtra("userUsername", username)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already on Account Screen", Toast.LENGTH_SHORT).show()
            }
        }*/
    }
}