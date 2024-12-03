package com.example.phishingapp

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

class ScreenCaptureService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var imageReader: ImageReader
    private val scanningScope = CoroutineScope(Dispatchers.Default)
    private val isScanning = AtomicBoolean(false)

    // MediaProjection callback to manage resources
    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.d(TAG, "MediaProjection stopped")
            stopScreenCapture()
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ScreenCaptureServiceChannel"
        private const val NOTIFICATION_ID = 1001
        const val TAG = "ScreenCaptureService"

        // Action constants for service control
        const val ACTION_START_PROJECTION = "ACTION_START_PROJECTION"
        const val ACTION_STOP_PROJECTION = "ACTION_STOP_PROJECTION"

        // Intent extra keys
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Screen Capture Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Screen Capture Active")
            .setContentText("Monitoring screen for potential threats")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PROJECTION -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                val resultData = intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    Log.d(TAG, "Initializing media projection")
                    initializeMediaProjection(resultCode, resultData)
                } else {
                    Log.e(TAG, "Invalid projection data. resultCode: $resultCode, resultData: $resultData")
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun initializeMediaProjection(resultCode: Int, resultData: Intent) {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)

        // Check if mediaProjection is null before proceeding
        if (mediaProjection == null) {
            Log.e(TAG, "Failed to initialize MediaProjection")
            stopSelf()
            return
        }

        // Register the callback BEFORE starting screen capture
        mediaProjection?.registerCallback(mediaProjectionCallback, null)

        startScreenCapture()
    }

    private fun startScreenCapture() {
        Log.d(TAG, "startScreenCapture: starting")
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val screenDensity = displayMetrics.densityDpi

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader.surface,
            null,
            null
        )

        startScanning()
    }

    private fun startScanning() {
        if (isScanning.compareAndSet(false, true)) {
            scanningScope.launch {
                while (isScanning.get()) {
                    try {
                        val image = imageReader.acquireLatestImage()
                        image?.let {
                            val bitmap = convertImageToBitmap(it)
                            val scanResult = performImageScanning(bitmap)

                            // Handle scan result on main thread
                            withContext(Dispatchers.Main) {
                                handleScanResult(scanResult)
                            }

                            it.close()
                        }
                        delay(1000) // Scan every second
                        Log.d(TAG, "startScanning: scanning now")
                    } catch (e: Exception) {
                        Log.e(TAG, "Scanning error", e)
                    }
                }
            }
        }
    }

    private fun convertImageToBitmap(image: android.media.Image): Bitmap {
        Log.d(TAG, "convertImageToBitmap: converting")
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width

        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    private fun performImageScanning(bitmap: Bitmap): ScanResult {
        Log.d(TAG, "performImageScanning: performing")
        // Implement your scanning logic here
        return ScanResult(
            isMalicious = bitmap.width > bitmap.height,
            description = "Sample scan result"
        )
    }

    private fun handleScanResult(result: ScanResult) {
        if (result.isMalicious) {
            showNotification("Potential Threat Detected", result.description)
        }
    }

    private fun showNotification(title: String, content: String) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun stopScreenCapture() {
        isScanning.set(false)
        scanningScope.cancel()

        // Unregister the callback
        mediaProjection?.unregisterCallback(mediaProjectionCallback)

        mediaProjection?.stop()
        virtualDisplay?.release()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopScreenCapture()
        super.onDestroy()
    }

    data class ScanResult(
        val isMalicious: Boolean,
        val description: String
    )
}