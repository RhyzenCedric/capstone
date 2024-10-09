package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var textViewSignup: TextView
    private lateinit var buttonGuest: Button // Guest button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignup = findViewById(R.id.textViewSignup)
        buttonGuest = findViewById(R.id.buttonGuest) // Find the guest button by its ID

        // Handle login button click
        buttonLogin.setOnClickListener {
            val userUsername = editTextUsername.text.toString().trim()
            val userPassword = editTextPassword.text.toString().trim()

            if (userUsername.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                if (userUsername == "user" && userPassword == "password") {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Handle guest button click
        buttonGuest.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Directly go to MainActivity
            startActivity(intent)
            finish()
        }

        // Navigate to sign-up screen
        textViewSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
