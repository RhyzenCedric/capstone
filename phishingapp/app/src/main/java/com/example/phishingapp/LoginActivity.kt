package com.example.phishingapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.phishingapp.backend.LoginRequest
import com.example.phishingapp.backend.LoginResponse
import com.example.phishingapp.backend.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
                loginUser(userUsername, userPassword)
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

        val signupText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(signupText)
        val signUpStart = signupText.indexOf("Sign Up")
        val signUpEnd = signUpStart + "Sign Up".length
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#108690")), signUpStart, signUpEnd, 0)

        textViewSignup.text = spannableString
    }

    // Function to log in the user
    private fun loginUser(userUsername: String, userPassword: String) {
        val loginRequest = LoginRequest(userUsername, userPassword)
        val call = RetrofitClient.instance.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LoginActivity", "Response received: ${response.body()}")
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        val userId = loginResponse.userId // Get userId from response
                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()

                        // Redirect to MainActivity with userId and username
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("userId", userId)
                        intent.putExtra("userUsername", userUsername)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorMessage = response.body()?.error ?: "Login failed: ${response.message()}"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
