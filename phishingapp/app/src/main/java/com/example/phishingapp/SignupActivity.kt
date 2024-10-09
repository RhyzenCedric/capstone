package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.phishingapp.backend.ApiService
import com.example.phishingapp.backend.SignupRequest
import com.example.phishingapp.backend.SignupResponse
import com.example.phishingapp.backend.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignupActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSignup = findViewById(R.id.buttonSignup)

        buttonSignup.setOnClickListener {
            val userUsername = editTextUsername.text.toString().trim()
            val userEmail = editTextEmail.text.toString().trim()
            val userPassword = editTextPassword.text.toString().trim()

            if (userUsername.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                signupUser(userUsername, userEmail, userPassword)
            }
        }
    }

    private fun signupUser(userUsername: String, userEmail: String, userPassword: String) {
        val signupRequest = SignupRequest(userUsername, userEmail, userPassword)

        RetrofitClient.instance.signup(signupRequest).enqueue(object : Callback<SignupResponse> {
            override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@SignupActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                    // Navigate to login screen after successful signup
                    startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@SignupActivity, "Signup failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                Toast.makeText(this@SignupActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
