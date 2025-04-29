package com.example.phishingapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.phishingapp.backend.LoginRequest
import com.example.phishingapp.backend.LoginResponse
import com.example.phishingapp.backend.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: ConstraintLayout
    private lateinit var textViewSignup: TextView
    private lateinit var buttonGuest: Button // Guest button
    private lateinit var passwordToggle: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewSignup = findViewById(R.id.textViewSignup)
        //buttonGuest = findViewById(R.id.buttonGuest) // Find the guest button by its ID
        passwordToggle = findViewById(R.id.passwordToggle)

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

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.invisible_icon)
            } else {
                editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.visible_icon)
            }
            editTextPassword.setSelection(editTextPassword.text.length) // Keep cursor position
        }

        // Handle guest button click
//        buttonGuest.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java) // Directly go to MainActivity
//            startActivity(intent)
//            finish()
//        }

        // Navigate to sign-up screen
        textViewSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        val signupText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(signupText)
        val signUpStart = signupText.indexOf("Sign Up")
        val signUpEnd = signUpStart + "Sign Up".length
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#1B1E46")), signUpStart, signUpEnd, 0)

        textViewSignup.text = spannableString
    }

    // Function to log in the user
    private fun loginUser (userUsername: String, userPassword: String) {
        val loginRequest = LoginRequest(userUsername, userPassword)
        val call = RetrofitClient.instance.login(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d("LoginActivity", "Response code: ${response.code()}")
                Log.d("LoginActivity", "Response headers: ${response.headers()}")

                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if (loginResponse != null) {
                        Log.d("LoginActivity", "Login successful: $loginResponse")
                        Toast.makeText(this@LoginActivity, loginResponse.message, Toast.LENGTH_SHORT).show()

                        // Save user data in SharedPreferences
                        val sharedPreferences = getSharedPreferences("User Data", MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putInt("userId", loginResponse.userId)
                            putString("userUsername", loginResponse.username)
                            apply()
                        }

                        // Redirect to MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.putExtra("userId", loginResponse.userId)
                        intent.putExtra("userUsername", loginResponse.username)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("LoginActivity", "Response body is null despite successful status")
                    }
                } else {
                    // Log the error response
                    val errorBody = response.errorBody()?.string()
                    Log.e("LoginActivity", "Error response: $errorBody")
                    val errorMessage = errorBody ?: "Login failed: ${response.message()}"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginActivity", "Error: ${t.message}")
                Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
