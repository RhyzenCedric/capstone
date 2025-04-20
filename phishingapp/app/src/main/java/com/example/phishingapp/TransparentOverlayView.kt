package com.example.phishingapp

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class TransparentOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "TransparentOverlayView"
    }

    var url: String? = null // Property to store the URL

    init {
        // For debugging, make the overlay slightly visible
        setBackgroundColor(Color.argb(128, 255, 0, 0)) // Semi-transparent red background
        // In production: setBackgroundColor(Color.TRANSPARENT)

        // Set click listener directly in the init block
        this.setOnClickListener {
            url?.let { fullUrl ->
                Log.d(TAG, "Overlay tapped for URL: $fullUrl")

                // Pause scanning and remove overlays by sending a broadcast
                LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(Intent(ScreenCaptureService.ACTION_PAUSE_AND_CLEAR))
//                LocalBroadcastManager.getInstance(context)
//                    .sendBroadcast(Intent(ScreenCaptureService.ACTION_STOP_SCANNING))

                // Create and show the alert dialog
                showPhishingAlertDialog(fullUrl)
            }
        }
    }

    private fun showPhishingAlertDialog(fullUrl: String) {
        val urlToReport = fullUrl

        // First, pause the service and remove all overlays
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(Intent(ScreenCaptureService.ACTION_PAUSE_AND_CLEAR))

        // Give the system time to remove overlays before showing dialog
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                // Create dialog with system alert window type
                val builder = AlertDialog.Builder(context.applicationContext, android.R.style.Theme_Material_Dialog_Alert)
                builder.apply {
                    setTitle("⚠️ Potential Phishing Link Detected")
                    setMessage("This link may be unsafe:\n\n$urlToReport\n\nHow would you like to proceed?")

                    setPositiveButton("Proceed Anyway") { _, _ ->
                        Log.d(TAG, "User chose to proceed to: $urlToReport")
                        resumeScanning()
                    }

                    setNeutralButton("Go Back") { _, _ ->
                        Log.d(TAG, "User chose to go back")
                        resumeScanning()
                    }

                    setNegativeButton("Report Link") { _, _ ->
                        Log.d(TAG, "User chose to report: $urlToReport")
                        reportLink(urlToReport)
                    }

                    setOnCancelListener {
                        Log.d(TAG, "Dialog canceled")
                        resumeScanning()
                    }

                    setCancelable(true)
                }

                val dialog = builder.create()

                // Use TYPE_APPLICATION_OVERLAY to ensure it appears over other windows
                dialog.window?.setType(android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)

                // Set focus flags to ensure dialog captures focus
                dialog.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)

                dialog.show()
            } catch (e: Exception) {
                Log.e(TAG, "Error showing dialog: ${e.message}")
                // Fallback to direct reporting if dialog fails
                reportLink(urlToReport)
                resumeScanning()
            }
        }, 300) // Give 300ms for overlay removal to complete
    }

    private fun resumeScanning() {
        // Resume scanning by sending a broadcast
        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(Intent(ScreenCaptureService.ACTION_RESUME_SCANNING))
    }

    private fun reportLink(url: String) {
        // Retrieve user data from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("userUsername", "Guest") ?: "Guest"
        val userId = sharedPreferences.getInt("userId", 0)

        // Create an intent to start the ReportActivity
        val intent = Intent(context, ReportActivity::class.java).apply {
            putExtra("userUsername", username)
            putExtra("userId", userId)
            putExtra("reportedLink", url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        context.startActivity(intent)
    }
}