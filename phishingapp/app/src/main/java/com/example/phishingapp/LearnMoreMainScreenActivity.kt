package com.example.phishingapp


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class LearnMoreMainScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn_more_main_screen) // Set the layout to phishing_infographic.xml

        // Set up the back button click listener
        findViewById<ImageView>(R.id.return_icon).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ScreenCaptureService.ACTION_RESUME_SCANNING))
            finish() // Close the current activity
        }

        // Set up click listeners for sections
        findViewById<View>(R.id.phishingSection).setOnClickListener {
            // Handle phishing section click
            // You can start another activity or show a dialog here
            val intent = Intent(this, PhishingInfographicActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.scammingSection).setOnClickListener {
            // Handle scamming section click
            // You can start another activity or show a dialog here
            val intent = Intent(this, ScammingInfographicActivity::class.java)
            startActivity(intent)
        }
    }
}