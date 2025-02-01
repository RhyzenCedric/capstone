package com.example.phishingapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report) // Make sure you have a layout with a TextView

       setupNavigationButtons()

        val username = intent.getStringExtra("userUsername") ?: "Guest"
        Log.d("ReportActivity", "Received username: $username")

        textViewUsername = findViewById(R.id.userUsername)

        textViewUsername.text = username

    }

    private fun setupNavigationButtons() {
        findViewById<Button>(R.id.button_nav_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username to MainActivity
            val username = intent.getStringExtra("userUsername") ?: "Guest"
            intent.putExtra("userUsername", username)

            startActivity(intent)
        }

        findViewById<Button>(R.id.button_nav_account).setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username to MainActivity
            val username = intent.getStringExtra("userUsername") ?: "Guest"
            intent.putExtra("userUsername", username)

            startActivity(intent)
        }
    }
}
