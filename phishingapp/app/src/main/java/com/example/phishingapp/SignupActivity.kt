package com.example.phishingapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
    private lateinit var buttonSignup: ConstraintLayout
    private lateinit var textViewLogin: TextView
    private lateinit var passwordToggle: ImageView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        editTextUsername = findViewById(R.id.editTextUsername)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonSignup = findViewById(R.id.buttonSignup)
        textViewLogin = findViewById(R.id.textViewLogin)
        passwordToggle = findViewById(R.id.passwordToggle)

        buttonSignup.setOnClickListener {
            val userUsername = editTextUsername.text.toString().trim()
            val userEmail = editTextEmail.text.toString().trim()
            val userPassword = editTextPassword.text.toString().trim()

            if (userUsername.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                signupUser(userUsername, userEmail, userPassword)
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

        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val loginText = "Already have an account? Log In"
        val spannableString = SpannableString(loginText)
        val logInStart = loginText.indexOf("Log In")
        val logInEnd = logInStart + "Log In".length
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#108690")), logInStart, logInEnd, 0)

        textViewLogin.text = spannableString
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
