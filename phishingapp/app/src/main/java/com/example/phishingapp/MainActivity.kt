package com.example.phishingapp

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingCircle: FloatingActionButton
    private lateinit var circleParams: WindowManager.LayoutParams
    private lateinit var removePopup: TextView
    private lateinit var removePopupParams: WindowManager.LayoutParams
    private lateinit var showCircleButton: Button // Track the button to enable/disable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_nav_home).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.button_nav_account).setOnClickListener {
            // Stay on the AccountActivity
            Toast.makeText(this, "Already on Account", Toast.LENGTH_SHORT).show()
        }

        // Check if overlay permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 100)
        } else {
            // If permission is granted, set up the button to create the floating circle
            setUpFloatingButton()
        }
    }

    // Handle the result of the permission request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Settings.canDrawOverlays(this)) {
                setUpFloatingButton()
            } else {
                Toast.makeText(this, "Overlay permission is required", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Set up the button that will create the floating circle
    private fun setUpFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Find the button in the layout and set up an OnClickListener
        showCircleButton = findViewById(R.id.button_activate_scanner)
        showCircleButton.setOnClickListener {
            createFloatingCircle()
            // Minimize the main activity window, but keep the app running in the background
        }
    }

    // Create the floating circle that can be dragged and snapped to edges
    private fun createFloatingCircle() {
        // Disable the button and change its text
        showCircleButton.isEnabled = false
        showCircleButton.text = "Scanner Activated"


        // Create a new FloatingActionButton
        floatingCircle = FloatingActionButton(this)
        floatingCircle.setImageResource(android.R.drawable.presence_online) // Use a small icon

        // Set layout parameters for the floating circle
        circleParams = WindowManager.LayoutParams(
            150, 150, // Size of the circle
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        circleParams.gravity = Gravity.TOP or Gravity.START
        circleParams.x = 0
        circleParams.y = 0

        // Create the "Remove?" popup at the bottom
        createRemovePopup()

        // Add a touch listener to make the circle draggable
        floatingCircle.setOnTouchListener(DraggableTouchListener())

        // Add a click listener to reopen the app when the floating circle is clicked
        floatingCircle.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        // Add the circle to the window
        windowManager.addView(floatingCircle, circleParams)
    }

    // Create the "Remove?" popup at the bottom of the screen
    private fun createRemovePopup() {
        removePopup = TextView(this).apply {
            text = "Remove?"
            textSize = 18f
            gravity = Gravity.CENTER
            setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
            setTextColor(resources.getColor(android.R.color.white))
            visibility = View.GONE
        }

        // Set layout parameters for the "Remove?" popup
        removePopupParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            200, // Height of the popup
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        removePopupParams.gravity = Gravity.BOTTOM

        // Add the "Remove?" popup to the window
        windowManager.addView(removePopup, removePopupParams)
    }

    // Touch listener to handle dragging and snapping the floating circle to the screen edges
    inner class DraggableTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private val removeThresholdPercentage = 0.75f // 75% of screen height

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Store initial touch positions
                    initialX = circleParams.x
                    initialY = circleParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Update the position of the circle while it's being dragged
                    circleParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    circleParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingCircle, circleParams)

                    // Get screen size to determine if the circle is in the lower part of the screen
                    val screenSize = Point()
                    windowManager.defaultDisplay.getSize(screenSize)

                    // Show "Remove?" popup only if the circle is dragged to the lower 25% of the screen
                    if (circleParams.y > screenSize.y * removeThresholdPercentage) {
                        removePopup.visibility = View.VISIBLE
                    } else {
                        removePopup.visibility = View.GONE
                    }
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    // Hide the "Remove?" popup when the user releases the circle
                    removePopup.visibility = View.GONE

                    // Get screen size to define the "Remove?" area
                    val screenSize = Point()
                    windowManager.defaultDisplay.getSize(screenSize)

                    // Define the boundaries of the "Remove?" area (only bottom part of the screen)
                    val removeAreaTop = screenSize.y - removePopup.height
                    val removeAreaBottom = screenSize.y // Bottom of the screen
                    val removeAreaLeft = 0
                    val removeAreaRight = screenSize.x // Right side of the screen

                    // Check if the circle is within the "Remove?" area
                    if (circleParams.y + floatingCircle.height >= removeAreaTop &&
                        circleParams.y <= removeAreaBottom &&
                        circleParams.x + floatingCircle.width >= removeAreaLeft &&
                        circleParams.x <= removeAreaRight
                    ) {
                        // Remove the floating circle if it's dragged into the "Remove?" area
                        windowManager.removeView(floatingCircle)

                        // Re-enable the button and change its text back
                        showCircleButton.isEnabled = true
                        showCircleButton.text = "Activate Scanner"
                    } else {
                        // Otherwise, snap the floating circle to the nearest edge (left or right)
                        circleParams.x = if (event.rawX < screenSize.x / 2) {
                            0 // Snap to left edge
                        } else {
                            screenSize.x - floatingCircle.width // Snap to right edge
                        }
                        windowManager.updateViewLayout(floatingCircle, circleParams)
                    }
                    return true
                }
            }
            return false
        }
    }
}
