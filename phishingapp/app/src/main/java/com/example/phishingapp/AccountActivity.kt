package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var buttonLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        // Get username passed from MainActivity
        val username = intent.getStringExtra("userUsername") ?: "Guest"

        // Log the username
        Log.d("AccountActivity", "Received username: $username")

        // Initialize UI elements
        textViewUsername = findViewById(R.id.textViewUsername)
        buttonLogout = findViewById(R.id.buttonLogout)

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
}