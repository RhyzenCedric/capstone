package com.example.phishingapp

import android.app.Activity
import android.content.BroadcastReceiver
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingCircle: View
    private lateinit var circleParams: WindowManager.LayoutParams
    private lateinit var removePopup: TextView
    private lateinit var removePopupParams: WindowManager.LayoutParams
    private lateinit var showCircleButton: ImageView
    private var isAppInBackground = false
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var isRed = false
    private var detectedMaliciousLink: String? = null
    private var userId by Delegates.notNull<Int>()
    private lateinit var userUsername: String


    companion object {
        const val TAG = "MainActivity"
        private const val MEDIA_PROJECTION_REQUEST_CODE = 101
    }

    override fun onPause() {
        super.onPause()
        // App goes into background
        isAppInBackground = true
    }

    override fun onResume() {
        super.onResume()
        // App comes to foreground
        isAppInBackground = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            val updatedUsername = it.getStringExtra("userUsername") ?: "DefaultUsername"
            Log.d(TAG, "Updated username received: $updatedUsername")
            // Update UI or perform actions with updatedUsername
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sonarWaveView = findViewById<SonarWaveView>(R.id.sonar_wave_view)
        val animationSwitch = findViewById<SwitchCompat>(R.id.animation_switch)

        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        userId = sharedPreferences.getInt("userId", -1) // Default value is -1 if not found
        userUsername = sharedPreferences.getString("userUsername", "") ?: ""

        // Use userId and userUsername as needed
        Log.d("MainActivity", "User  ID: $userId, Username: $userUsername")

        // Set initial state (animation enabled by default)
        sonarWaveView.setAnimationEnabled(true)
        animationSwitch.isChecked = true

        // Set up listener for the switch
        animationSwitch.setOnCheckedChangeListener { _, isChecked ->
            sonarWaveView.setAnimationEnabled(isChecked)
        }



        // Set up navigation buttons (same as previous implementation)
        setupNavigationButtons()

        // Initialize MediaProjection Manager
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE)
                as MediaProjectionManager

        val intentFilter = IntentFilter().apply {
            addAction("com.example.phishingapp.MALICIOUS_LINK_DETECTED")
            addAction("com.example.phishingapp.NO_MALICIOUS_LINKS")
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            maliciousLinkReceiver,
            intentFilter
        )

        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            setupFloatingButton()
        }
    }



    private val maliciousLinkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "com.example.phishingapp.MALICIOUS_LINK_DETECTED" -> {
                    detectedMaliciousLink = intent.getStringExtra("maliciousLink") ?: "Unknown Link" // ✅ Corrected

                    changeFloatingCircleColorToRed()

                    // Notify user
                    //Toast.makeText(this@MainActivity, "Phishing Link Detected: $detectedMaliciousLink", Toast.LENGTH_LONG).show()
                }
                "com.example.phishingapp.NO_MALICIOUS_LINKS" -> {
                    detectedMaliciousLink = null
                    changeFloatingCircleColorToBlue()
                }
            }
        }
    }



    private fun setupNavigationButtons() {
        findViewById<ConstraintLayout>(R.id.button_account).setOnClickListener {
            if (javaClass != AccountActivity::class.java) {
                // Retrieve the updated username from the intent or shared preferences
                val updatedUsername = intent.getStringExtra("userUsername") ?: getSharedPreferences("User Data", Context.MODE_PRIVATE).getString("userUsername", "Guest")
                val userId = intent.extras?.getInt("userId") ?: getSharedPreferences("User Data", Context.MODE_PRIVATE).getInt("userId", 0)
                Log.d(TAG, "Passing userId: $userId")
                val intent = Intent(this@MainActivity, AccountActivity::class.java)
                intent.putExtra("userUsername", updatedUsername)
                intent.putExtra("userId", userId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already on Account Screen", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ConstraintLayout>(R.id.button_reports).setOnClickListener {
            if (javaClass != ReportActivity::class.java) {
                // Retrieve the updated username from the intent or shared preferences
                val updatedUsername = intent.getStringExtra("userUsername") ?: getSharedPreferences("User Data", Context.MODE_PRIVATE).getString("userUsername", "Guest")
                val userId = intent.extras?.getInt("userId") ?: getSharedPreferences("User Data", Context.MODE_PRIVATE).getInt("userId", 0)
                Log.d(TAG, "Passing userId: $userId")
                val intent = Intent(this@MainActivity, ReportActivity::class.java)
                intent.putExtra("userUsername", updatedUsername)
                intent.putExtra("userId", userId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already on Report Screen", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ConstraintLayout>(R.id.button_learn_more).setOnClickListener {
            if (javaClass != LearnMoreActivity::class.java) {
                // Retrieve the updated username from the intent or shared preferences
                val updatedUsername = intent.getStringExtra("userUsername") ?: getSharedPreferences("User Data", Context.MODE_PRIVATE).getString("userUsername", "Guest")
                val userId = intent.extras?.getInt("userId") ?: getSharedPreferences("User Data", Context.MODE_PRIVATE).getInt("userId", 0)
                Log.d(TAG, "Passing userId: $userId")
                val intent = Intent(this@MainActivity, LearnMoreActivity::class.java)
                intent.putExtra("userUsername", updatedUsername)
                intent.putExtra("userId", userId)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already on Report Screen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestOverlayPermission() {
// Check and request overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 0)
            return
        }

// Check and request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the request code matches and ensure that resultCode indicates success
        if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // Check if the data is not null
                if (data != null) {
                    Log.d(TAG, "onActivityResult: starting foreground service")
                    // Start the foreground service with projection data
                    val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
                        action = ScreenCaptureService.ACTION_START_PROJECTION
                        putExtra(ScreenCaptureService.EXTRA_RESULT_CODE, resultCode)
                        putExtra(ScreenCaptureService.EXTRA_RESULT_DATA, data)
                    }
                    Log.d(TAG, "Result Code: $resultCode")
                    Log.d(TAG, "Intent data: ${data?.extras}")
                    data.extras?.keySet()?.forEach {
                        Log.d(ScreenCaptureService.TAG, "Key: $it, Value: ${data.extras?.get(it)}")
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }

                    // Create floating circle after permission is granted
                    createFloatingCircle()
                } else {
                    // Display a message if data is null, which is unexpected
                    Log.e(TAG, "onActivityResult: Screen capture data is null", )
                    Toast.makeText(this, "Screen capture data is null", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Handle the case when the result is not OK
                Toast.makeText(this, "Screen capture permission denied or failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        showCircleButton = findViewById(R.id.button_activate_scanner)
        showCircleButton.setOnClickListener {
            requestScreenCapturePermission()
        }
    }

    private fun createFloatingCircle() {
        // Disable the button and change its text
        showCircleButton.isEnabled = false
        showCircleButton.setImageResource(R.drawable.home_activated)


        // Create a new View for the floating circle
        floatingCircle = View(this)

        val circleDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.argb(200, 0, 0, 255)) // Initial Blue
        }


        floatingCircle.background = circleDrawable

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
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningTasks = activityManager.appTasks

            // Check if the app is in the background
            val isInBackground = runningTasks.none {
                it.taskInfo.baseActivity?.className == MainActivity::class.java.name
            }

            if (isInBackground) {
                // App is minimized or in the background, redirect to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            } else {
                // App is already on the home screen, display a toast
                Toast.makeText(this, "Already on Home Screen", Toast.LENGTH_SHORT).show()
            }
        }

        // Add the circle to the window
        windowManager.addView(floatingCircle, circleParams)
    }

    private fun changeFloatingCircleColorToRed() {
        val circleDrawable = floatingCircle.background as GradientDrawable
        circleDrawable.setColor(Color.argb(200, 255, 20, 20))// Change to red
        isRed= true

    }

    private fun changeFloatingCircleColorToBlue() {
        if (::floatingCircle.isInitialized) {
            val circleDrawable = floatingCircle.background as GradientDrawable
            circleDrawable.setColor(Color.argb(200, 0, 0, 255)) // Original blue color
            isRed=false
        }
    }

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

    inner class DraggableTouchListener : View.OnTouchListener {
        private var initialX = 0
        private var initialY = 0
        private var initialTouchX = 0f
        private var initialTouchY = 0f
        private val removeThresholdPercentage = 0.75f // 75% of screen height
        private val clickThreshold = 10 // Distance threshold for detecting a tap

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
                    val screenSize = android.graphics.Point()
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

                    val deltaX = Math.abs(event.rawX - initialTouchX)
                    val deltaY = Math.abs(event.rawY - initialTouchY)
                    if (deltaX < clickThreshold && deltaY < clickThreshold) {
                        handleCircleTap() // Call a method to handle the tap functionality
                        return true
                    }

                    // Get screen size to define the "Remove?" area
                    val screenSize = android.graphics.Point()
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
                    )  {
                        // Change the image to home_activate before removing the view
                        showCircleButton.setImageResource(R.drawable.home_activate)

                        // Delay the removal slightly to allow the UI to update
                        floatingCircle.postDelayed({
                            try {
                                windowManager.removeView(floatingCircle)
                                windowManager.removeView(removePopup)
                            } catch (e: IllegalArgumentException) {
                                Log.e(ScreenCaptureService.TAG, "Error removing views", e)
                            }
                        }, 100) // Short delay (100ms) to ensure image update

                        // Stop the scanning service
                        stopScanning()
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

    private fun handleCircleTap() {
        if (isRed) {
            handleCircleTapReport()
       } else {
            handleCircleTapHome()
        }
    }

    private fun handleCircleTapHome() {
        if (isAppInBackground) {
            val username = intent.getStringExtra("userUsername") ?: "Guest"
            val userId = intent.extras?.getInt("userId")

            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("userUsername", username)
                putExtra("userId", userId)
                //putExtra("reportedLink", detectedMaliciousLink) // ✅ Pass the stored malicious link
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Already on Home Screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleCircleTapReport() {
//        if (isAppInBackground) {
//            val username = intent.getStringExtra("userUsername") ?: "Guest"
//            val userId = intent.extras?.getInt("userId")
//
//            val intent = Intent(this, ReportActivity::class.java).apply {
//                putExtra("userUsername", username)
//                putExtra("userId", userId)
//                putExtra("reportedLink", detectedMaliciousLink) // ✅ Pass the stored malicious link
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            }
//            startActivity(intent)
//        } else {
//            val username = intent.getStringExtra("userUsername") ?: "Guest"
//            val userId = intent.extras?.getInt("userId")
//
//            val intent = Intent(this, ReportActivity::class.java).apply {
//                putExtra("userUsername", username)
//                putExtra("userId", userId)
//                putExtra("reportedLink", detectedMaliciousLink) // ✅ Pass the stored malicious link
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            }
//            startActivity(intent)
//        }
        if (isAppInBackground){
            Toast.makeText(this, "A Malicious link was found!", Toast.LENGTH_SHORT).show()
        }
   }



    private fun requestScreenCapturePermission() {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, MEDIA_PROJECTION_REQUEST_CODE)
    }

    override fun onDestroy() {
        super.onDestroy()

        // Comprehensive service and view cleanup
        stopScanning()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(maliciousLinkReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        // Remove views safely
        try {
            if (::floatingCircle.isInitialized) {
                windowManager.removeView(floatingCircle)
            }
            if (::removePopup.isInitialized) {
                windowManager.removeView(removePopup)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(ScreenCaptureService.TAG, "Error removing views in onDestroy", e)
        }
    }

    private fun stopScanning() {
        // Stop the ScreenCaptureService completely
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ScreenCaptureService.ACTION_STOP_PROJECTION
        }

        try {
            // Stop the service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            // Additional logging for debugging
            Log.d(ScreenCaptureService.TAG, "Stopping ScreenCaptureService from MainActivity")
        } catch (e: Exception) {
            Log.e(ScreenCaptureService.TAG, "Error stopping ScreenCaptureService", e)
            Toast.makeText(this, "Failed to stop scanning service", Toast.LENGTH_SHORT).show()
        }

        // Reset UI elements
        if (::showCircleButton.isInitialized) {
            showCircleButton.isEnabled = true
        }
    }

}