package com.example.phishingapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class LearnMoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_more_main_screen) // <-- still your XML file name

        // Find the sections (now ConstraintLayouts)
        val phishingSection = findViewById<ConstraintLayout>(R.id.phishingSection)
        val smishingSection = findViewById<ConstraintLayout>(R.id.smishingSection)
        val scammingSection = findViewById<ConstraintLayout>(R.id.scammingSection)

        setupNavigationButtons()

        // Set click listeners
        phishingSection.setOnClickListener {
            //openInfographic("phishing")
        }

        smishingSection.setOnClickListener {
            //openInfographic("smishing")
        }

        scammingSection.setOnClickListener {
            //openInfographic("scamming")
        }
    }

//    private fun openInfographic(category: String) {
//        val intent = Intent(this, InfographicActivity::class.java)
//        intent.putExtra("CATEGORY", category)
//        startActivity(intent)
//    }

    private fun setupNavigationButtons(){
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // Pass the username to MainActivity
            val username = intent.getStringExtra("userUsername") ?: "Guest"
            intent.putExtra("userUsername", username)

            startActivity(intent)
        }
    }
}
