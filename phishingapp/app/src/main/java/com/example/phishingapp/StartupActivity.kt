package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class StartupActivity : AppCompatActivity() {
    private lateinit var buttonLogin: ConstraintLayout
    private lateinit var buttonSignUp: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.startup_activity) // Make sure this layout exists
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonSignUp = findViewById(R.id.buttonSignup)

        buttonLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java) // Directly go to MainActivity
            startActivity(intent)
            finish()
        }

        buttonSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java) // Directly go to MainActivity
            startActivity(intent)
            finish()
        }


    }
}
