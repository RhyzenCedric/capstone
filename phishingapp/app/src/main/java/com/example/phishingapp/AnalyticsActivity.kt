package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class AnalyticsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        setupNavigationButtons()
        // Get username passed from MainActivity
        val username = intent.getStringExtra("userUsername") ?: "Guest"
        val userId = intent.extras?.getInt("userId", -1)

        // Log the username
        Log.d("AnalyticsActivity", "Received username: $username")
        Log.d("AnalyticsActivity", "User ID: $userId")


    }

    private fun setupNavigationButtons() {
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username to MainActivity
            val username = intent.getStringExtra("userUsername") ?: "Guest"
            intent.putExtra("userUsername", username)

            startActivity(intent)
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