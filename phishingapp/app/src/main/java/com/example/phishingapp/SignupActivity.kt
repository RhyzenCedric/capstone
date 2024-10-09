package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignup: Button
    private lateinit var textViewLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSignup = findViewById(R.id.buttonSignup)
        textViewLogin = findViewById(R.id.textViewLogin)

        // Handle signup button click
        buttonSignup.setOnClickListener {
            val userUsername = editTextUsername.text.toString().trim()
            val userEmail = editTextEmail.text.toString().trim()
            val userPassword = editTextPassword.text.toString().trim()

            if (userUsername.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Add your signup logic here
                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()

                // After signing up, redirect to login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // Navigate to login screen
        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
