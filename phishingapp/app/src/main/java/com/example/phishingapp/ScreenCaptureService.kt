package com.example.phishingapp

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException
import java.util.concurrent.ExecutionException

class ScreenCaptureService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var imageReader: ImageReader
    private var scanningScope = CoroutineScope(Dispatchers.Default)
    private val isScanning = AtomicBoolean(false)
    private lateinit var windowManager: WindowManager
    private val overlays = mutableMapOf<String, Pair<View, Pair<Int, Int>>>() // URL -> (View, Coordinates)
    private var wasAppInForeground = false // Track the previous state
    private val visibleLinks = mutableSetOf<String>() // Track currently visible links
    private val isPaused = AtomicBoolean(false)
    private var isScanningPaused = false
    private var isServicePaused = false
    private var truePositives = 0
    private var trueNegatives = 0
    private var falsePositives = 0
    private var falseNegatives = 0

    // Store link positions with coordinates
    data class LinkPosition(val url: String, val boundingBox: Rect)
    private val currentLinkPositions = mutableMapOf<String, LinkPosition>()

    private val mediaProjectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.d(TAG, "MediaProjection stopped")
            stopScreenCapture()
        }
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "ScreenCaptureServiceChannel"
        private const val NOTIFICATION_ID = 1001
        private const val MALICIOUS_LINKS_NOTIFICATION_ID = 1002
        const val TAG = "ScreenCaptureService"

        const val ACTION_START_PROJECTION = "ACTION_START_PROJECTION"
        const val ACTION_STOP_PROJECTION = "ACTION_STOP_PROJECTION"
        const val ACTION_PAUSE_SCANNING = "com.example.phishingapp.ACTION_PAUSE_SCANNING"
        const val ACTION_RESUME_SCANNING = "com.example.phishingapp.ACTION_RESUME_SCANNING"
        const val ACTION_PAUSE_AND_CLEAR = "com.example.phishingapp.ACTION_PAUSE_AND_CLEAR"
        const val ACTION_STOP_SCANNING = "com.example.phishingapp.ACTION_STOP_SCANNING"

        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_RESULT_DATA = "resultData"
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val dp = Array(m + 1) { IntArray(n + 1) }

        // Initialize first row and column
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j

        // Fill the rest of the matrix
        for (i in 1..m) {
            for (j in 1..n) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // Deletion
                    dp[i][j - 1] + 1,      // Insertion
                    dp[i - 1][j - 1] + cost // Substitution
                )
            }
        }

        return dp[m][n]
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("userUsername", "Guest") ?: "Guest"
        val userId = sharedPreferences.getInt("userId", 0)
        Log.d(TAG, "Retrieved Username: $username")
        Log.d(TAG, "Retrieved User ID: $userId")

        val filter = IntentFilter().apply {
            addAction(ACTION_PAUSE_SCANNING)
            addAction(ACTION_RESUME_SCANNING)
            addAction(ACTION_PAUSE_AND_CLEAR)
            addAction(ACTION_STOP_SCANNING)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            scanControlReceiver,
            filter
        )
    }

    private val scanControlReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PAUSE_SCANNING -> {
                    Log.d(TAG, "Received request to pause scanning")
                    isPaused.set(true)
                }
                ACTION_PAUSE_AND_CLEAR -> {
                    Log.d(TAG, "Received request to pause scanning and clear overlays")
                    isPaused.set(true)
                    isServicePaused = true
                    removeAllOverlays()
                    stopScanning()
                }
                ACTION_RESUME_SCANNING -> {
                    Log.d(TAG, "Received request to resume scanning")
                    isPaused.set(false)
                    isServicePaused = false
                    visibleLinks.clear()
                    currentLinkPositions.clear()
                    // Reset the coroutine scope
                    scanningScope = CoroutineScope(Dispatchers.Default)
                    startScanning() // Resume scanning
                }
            }
        }
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
        try {
            showNotification("Screen Capture Active", "Monitoring screen for potential threats")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting foreground service", e)
            stopSelf()
        }
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
            ACTION_STOP_PROJECTION -> {
                Log.d(TAG, "Stopping screen capture service")
                stopScreenCapture()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun initializeMediaProjection(resultCode: Int, resultData: Intent) {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)

        if (mediaProjection == null) {
            Log.e(TAG, "Failed to initialize MediaProjection")
            stopSelf()
            return
        }

        mediaProjection?.registerCallback(mediaProjectionCallback, null)
        startScreenCapture()
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(1)

        if (runningTasks.isNotEmpty()) {
            val topActivity = runningTasks[0].topActivity
            val packageName = topActivity?.packageName

            // First check if this is our package
            if (packageName == packageName) {
                // It's our app's package, now check if it's one of our main activities
                val mainActivities = listOf(
                    "com.example.phishingapp.ReportActivity",
                    "com.example.phishingapp.AccountActivity",
                    "com.example.phishingapp.SignupActivity",
                    "com.example.phishingapp.LoginActivity",
                    "com.example.phishingapp.StartupActivity",
                    "com.example.phishingapp.AnalyticsActivity",

                )

                // Return true only if it's one of our main activities
                return mainActivities.any { activityName ->
                    topActivity?.className == activityName
                }
            }

            // It's not our package at all, so our app is not in foreground
            return false
        }

        // No running tasks, so definitely not in foreground
        return false
    }

    private fun startScreenCapture() {
        Log.d(TAG, "startScreenCapture: starting")
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val screenDensity = displayMetrics.densityDpi

        // Log the screen dimensions
        Log.d(TAG, "Screen Dimensions: width=$screenWidth, height=$screenHeight, density=$screenDensity")

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        // Release any existing virtual display
        virtualDisplay?.release()
        virtualDisplay = null

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
                    if (isServicePaused) {
                        Log.d(TAG, "Service is paused, exiting scanning loop")
                        isScanning.set(false)
                        return@launch
                    }
                    try {
                        // Check if scanning is paused
                        if (isPaused.get() || isScanningPaused) {
                            delay(100) // Check pause state every 0.1 seconds
                            continue
                        }

                        // Check app foreground status
                        val isInForeground = isAppInForeground()

                        // Handle state transitions
                        if (isInForeground != wasAppInForeground) {
                            withContext(Dispatchers.Main) {
                                if (isInForeground) {
                                    // App just came to foreground, remove overlays
                                    Log.d(TAG, "App is now in the foreground, temporarily removing overlays")
                                    removeAllOverlays()
                                } else {
                                    // App just went to background, reset tracking
                                    Log.d(TAG, "App is now in the background, resuming overlay creation")
                                    visibleLinks.clear()
                                    currentLinkPositions.clear()
                                }
                            }
                            // Update the state tracking variable
                            wasAppInForeground = isInForeground
                        }

                        // If app is in foreground, pause the actual scanning
                        if (isInForeground) {
                            delay(1000) // Wait for 1 second before checking again
                            continue
                        }

                        // Perform scanning when app is in background
                        val image = imageReader.acquireLatestImage()
                        if (image != null) {
                            try {
                                val bitmap = convertImageToBitmap(image)
                                val scanResults = performImageScanning(bitmap)

                                withContext(Dispatchers.Main) {
                                    handleScanResult(scanResults, bitmap)
                                }
                            } finally {
                                image.close() // Ensure the image is closed after processing
                            }
                        } else {
                            Log.d(TAG, "No image acquired from ImageReader")
                        }
                        delay(500) // Scan every 0.5 seconds
                        Log.d(TAG, "startScanning: scanning now")
                    } catch (e: Exception) {
                        Log.e(TAG, "Scanning error", e)
                    }
                }
            }
        }
    }

    private fun stopScanning() {
        Log.d(TAG, "Stopping scanning")
        isScanning.set(false)
        scanningScope.cancel() // Cancel the coroutine scope
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

        // Log the bitmap dimensions
        Log.d(TAG, "Bitmap Dimensions: width=${bitmap.width}, height=${bitmap.height}")

        return bitmap
    }

    private data class ScanningResult(
        val scanResults: List<ScanResult>,
        val linkPositions: List<LinkPosition>
    )

    private fun performImageScanning(bitmap: Bitmap): ScanningResult {
        Log.d(TAG, "performImageScanning: performing link scanning")

        try {
            val textRecognitionResult = performOCR(bitmap)
            val extractedText = textRecognitionResult.first
            val textElements = textRecognitionResult.second

            // New list to track currently visible links in this scan
            val newVisibleLinks = mutableSetOf<String>()
            val newLinkPositions = mutableListOf<LinkPosition>()
            val scanResults = mutableListOf<ScanResult>()

            // Process each text element that might contain URLs
            textElements.forEach { (text, boundingBox) ->
                val urls = extractUrls(text)

                urls.forEach { urlString ->
                    val scanResult = scanUrl(urlString)
                    scanResults.add(scanResult)

                    if (scanResult.isMalicious) {
                        newVisibleLinks.add(urlString)
                        newLinkPositions.add(LinkPosition(urlString, boundingBox))
                        Log.d(TAG, "Detected malicious URL: $urlString at position: $boundingBox")
                    }
                }
            }

            // Store the newly visible links
            visibleLinks.clear()
            visibleLinks.addAll(newVisibleLinks)

            return ScanningResult(scanResults, newLinkPositions)
        } catch (e: Exception) {
            Log.e(TAG, "URL scanning error", e)
            return ScanningResult(
                listOf(
                    ScanResult(
                        url = "Unknown",
                        isMalicious = false,
                        description = "Scanning error occurred"
                    )
                ),
                emptyList()
            )
        }
    }

    private fun performOCR(bitmap: Bitmap): Pair<String, List<Pair<String, Rect>>> {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        return try {
            val result = Tasks.await(recognizer.process(image))
            val extractedText = result.textBlocks.joinToString("\n") { block ->
                block.text
            }

            // Extract text elements with their bounding boxes
            val textElements = result.textBlocks.flatMap { block ->
                block.lines.flatMap { line ->
                    line.elements.mapNotNull { element ->
                        element.boundingBox?.let { box ->
                            Pair(element.text, box)
                        }
                    }
                }
            }

            Log.d(TAG, "OCR extracted text elements: ${textElements.size}")
            Pair(extractedText, textElements)
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit OCR error", e)
            Pair("", emptyList())
        } finally {
            recognizer.close()
        }
    }

    private fun mapBoundingBoxToScreen(bitmap: Bitmap, boundingBox: Rect): Rect {
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Calculate scale factors
        val scaleX = screenWidth.toFloat() / bitmap.width
        val scaleY = screenHeight.toFloat() / bitmap.height

        // Scale the bounding box to screen coordinates
        val screenBoundingBox = Rect(
            (boundingBox.left * scaleX).toInt(),
            (boundingBox.top * scaleY).toInt(),
            (boundingBox.right * scaleX).toInt(),
            (boundingBox.bottom * scaleY).toInt()
        )

        Log.d(TAG, "Mapped bounding box: $screenBoundingBox")
        return screenBoundingBox
    }

    private fun extractUrls(text: String): List<String> {
        val urlMatcher = Patterns.WEB_URL
        val matcher = urlMatcher.matcher(text)
        val urls = mutableListOf<String>()

        while (matcher.find()) {
            var url = matcher.group()

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }

            urls.add(url)
        }

        return urls
    }

    private fun sanitizeUrl(urlString: String): String {
        var cleanUrl = urlString.trim()

        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "http://$cleanUrl"
        }

        return cleanUrl
    }

    private fun scanUrl(urlString: String): ScanResult {
        return try {
            val sanitizedUrl = sanitizeUrl(urlString)
            val url = URL(sanitizedUrl)
            val hostname = url.host.lowercase()
            val tld = hostname.substringAfterLast('.', "")

            // Define lists for analysis
            val phishingIndicators = listOf(
                "free-gift",
                "suspicious",
                "login-verify",
                "account-secure",
                "urgent-action"
            )

            val suspiciousDomains = listOf(
                "suspicious-link.com",
                "fake-bank.net",
                "phishing-site.org"
            )

            val legitimateDomains = listOf(
                "google.com",
                "microsoft.com",
                "paypal.com",
                "amazon.com",
                "facebook.com",
                "twitter.com",
                "apple.com",
                "bank.com",
                "open.spotify.com",
                "youtube.com"
            )

            // Whitelist of known safe domains
            val safeDomains = listOf(
                "messenger.com",
                "facebook.com",
                "whatsapp.com"
            )

            // Check if the URL is in the safe domains
            if (safeDomains.any { hostname.contains(it) }) {
                return ScanResult(url = sanitizedUrl, isMalicious = false, description = "Known safe domain")
            }

            // TLD Risk Assessment
            val riskyCcTlds = listOf(
                "tk", "ml", "ga", "cf", "gq", // Free TLDs often used for scams
                "pw", "top", "xyz",           // Frequently associated with spam
                "loan", "win", "bid"          // Suspicious commercial TLDs
            )

            val isSuspiciousTld = tld in riskyCcTlds

            // Enhanced URL Complexity and Risk Checks
            val hasPhishingKeyword = phishingIndicators.any {
                hostname.contains(it) || sanitizedUrl.contains(it)
            }

            val isKnownBadDomain = suspiciousDomains.any {
                hostname.contains(it)
            }

            val isLongAndComplexUrl = sanitizedUrl.let {
                it.length > 20 ||                        // Very long URL
                        (it.count { char -> char == '.' } > 3) || // Too many subdomains
                        (it.count { char -> char == '/' } > 4) || // Too many path segments
                        (it.count { char -> char == '-' } > 2)    // Multiple hyphens
            }

            // Subdomain obfuscation check
            val hasObfuscatedSubdomain = hostname.split('.').let { parts ->
                parts.size > 2 && parts.first().length < 3
            }

            // Levenshtein distance check for domain similarity
            val domainSimilarityRisk = legitimateDomains.any { legitimateDomain ->
                val distance = levenshteinDistance(hostname, legitimateDomain)
                val similarityRatio = distance.toDouble() / maxOf(hostname.length, legitimateDomain.length)

                // If the similarity ratio is less than 0.3 (30% difference), consider it a potential phishing attempt
                similarityRatio < 0.3
            }

            // Comprehensive risk assessment
            val isMalicious = hasPhishingKeyword ||
                    isKnownBadDomain ||
                    isLongAndComplexUrl ||
                    domainSimilarityRisk ||
                    isSuspiciousTld ||
                    hasObfuscatedSubdomain

            ScanResult(
                url = sanitizedUrl,
                isMalicious = isMalicious,
                description = when {
                    isSuspiciousTld -> "Suspicious top-level domain"
                    hasPhishingKeyword -> "Suspicious keyword detected"
                    isKnownBadDomain -> "Known malicious domain"
                    isLongAndComplexUrl -> "Overly complex URL structure"
                    hasObfuscatedSubdomain -> "Potential subdomain obfuscation"
                    domainSimilarityRisk -> "Potential domain spoofing detected"
                    else -> "Link appears safe"
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "URL scanning error for $urlString", e)
            ScanResult(
                url = urlString,
                isMalicious = false,
                description = "Invalid URL"
            )
        }
    }

    private fun handleScanResult(scanningResult: ScanningResult, bitmap: Bitmap) {
        if (wasAppInForeground) {
            Log.d(TAG, "App is in foreground, skipping overlay handling")
            return
        }

        val maliciousLinks = scanningResult.scanResults.filter { it.isMalicious }
        val maliciousLinksDetected = maliciousLinks.isNotEmpty()

        // Update metrics
        if (maliciousLinksDetected) {
            truePositives += maliciousLinks.size
            falseNegatives += currentLinkPositions.size - maliciousLinks.size
        } else {
            // Check for false positives
            val visibleLinksNotDetected = visibleLinks.filter { url ->
                !scanningResult.scanResults.any { it.url == url && it.isMalicious }
            }
            falsePositives += visibleLinksNotDetected.size
            trueNegatives += visibleLinksNotDetected.size
        }

        // Log the current metrics
        Log.d(TAG, "TP: $truePositives, TN: $trueNegatives, FP: $falsePositives, FN: $falseNegatives")

        // Calculate and display accuracy
        calculateAndDisplayAccuracy()
        // Find links that disappeared (were visible before but not now)
        val disappearedLinks = overlays.keys.filter { url ->
            !visibleLinks.contains(url)
        }

        // Remove overlays for links that disappeared
        disappearedLinks.forEach { url ->
            removeOverlay(url)
            Log.d(TAG, "Removed overlay for disappeared link: $url")
        }

        // Update or create overlays for visible links
        scanningResult.linkPositions.forEach { linkPosition ->
            val url = linkPosition.url
            val boundingBox = linkPosition.boundingBox
            val screenBoundingBox = mapBoundingBoxToScreen(bitmap, boundingBox)

            // Check if this link already has an overlay but at a different position
            if (overlays.containsKey(url)) {
                val oldCoords = overlays[url]!!.second
                val newCoords = Pair(screenBoundingBox.left, screenBoundingBox.top)

                // If position changed significantly, update the overlay
                if (hasPositionChangedSignificantly(oldCoords, newCoords)) {
                    Log.d(TAG, "Link position changed for $url: $oldCoords -> $newCoords")
                    removeOverlay(url)
                    addOverlay(url, screenBoundingBox)
                }
            } else {
                // Add new overlay
                addOverlay(url, screenBoundingBox)
            }

            // Store the current position
            currentLinkPositions[url] = linkPosition
        }

        if (maliciousLinksDetected) {
            val firstMaliciousLink = maliciousLinks.first().url

            val intent = Intent("com.example.phishingapp.MALICIOUS_LINK_DETECTED")
            intent.putExtra("maliciousLink", firstMaliciousLink)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            triggerVibration()
            Log.d(TAG, "⚠️ Triggered haptic feedback due to malicious link")

            showNotification(
                "⚠️ Phishing Link Detected",
                "Malicious link: $firstMaliciousLink",
                maliciousLinks
            )
        } else {
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent("com.example.phishingapp.NO_MALICIOUS_LINKS"))
        }
    }

    private fun calculateAndDisplayAccuracy() {
        val total = truePositives + trueNegatives + falsePositives + falseNegatives
        val precision = if (truePositives + falsePositives > 0) {
            (truePositives.toDouble() / (truePositives + falsePositives)) * 100
        } else {
            0.0
        }

        val recall = if (truePositives + falseNegatives > 0) {
            (truePositives.toDouble() / (truePositives + falseNegatives)) * 100
        } else {
            0.0
        }

        val f1Score = if (precision + recall > 0) {
            (2 * precision * recall) / (precision + recall)
        } else {
            0.0
        }

        val accuracy = if (total > 0) {
            ((truePositives + trueNegatives).toDouble() / total) * 100
        } else {
            0.0
        }

        // Display the metrics in the notification
        showAccuracyNotification(
            accuracy,
            precision,
            recall,
            f1Score,
            truePositives,
            trueNegatives,
            falsePositives,
            falseNegatives
        )

        Log.d(TAG, "Accuracy: ${accuracy.toInt()}%")
        Log.d(TAG, "Precision: ${precision.toInt()}%")
        Log.d(TAG, "Recall: ${recall.toInt()}%")
        Log.d(TAG, "F1 Score: ${f1Score.toInt()}")
    }

    // Determine if position changed significantly enough to warrant updating the overlay
    private fun hasPositionChangedSignificantly(oldCoords: Pair<Int, Int>, newCoords: Pair<Int, Int>): Boolean {
        val xDiff = Math.abs(oldCoords.first - newCoords.first)
        val yDiff = Math.abs(oldCoords.second - newCoords.second)

        // If position changed by more than 10 pixels in either direction
        return xDiff > 10 || yDiff > 10
    }

    private fun addOverlay(url: String, boundingBox: Rect) {
        Log.d(TAG, "Adding overlay for URL: $url at position: $boundingBox")

        val overlayView = TransparentOverlayView(this)
        overlayView.url = url // Set the URL property

        // No need for a custom click listener anymore as we're handling it in the view

        val layoutParams = WindowManager.LayoutParams(
            boundingBox.width(),
            boundingBox.height(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        // Position the overlay based on the bounding box
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = boundingBox.left
        layoutParams.y = boundingBox.top - 50// Adjust vertical position as needed

        try {
            windowManager.addView(overlayView, layoutParams)
            // Store the overlay with its coordinates
            overlays[url] = Pair(overlayView, Pair(boundingBox.left, boundingBox.top))
        } catch (e: Exception) {
            Log.e(TAG, "Error adding overlay: ${e.message}")
        }
    }

    private fun removeOverlay(url: String) {
        overlays[url]?.let { (view, _) ->
            try {
                windowManager.removeView(view)
                overlays.remove(url)
                Log.d(TAG, "Removed overlay for URL: $url")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay: ${e.message}")
            }
        }
    }

    private fun removeAllOverlays() {
        Log.d(TAG, "Removing all overlays (total: ${overlays.size})")

        // Make a copy of the overlays to avoid concurrent modification
        val overlaysCopy = HashMap(overlays)

        overlaysCopy.forEach { (url, pair) ->
            try {
                val view = pair.first
                windowManager.removeView(view)
                Log.d(TAG, "Successfully removed overlay for: $url")
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay: ${e.message}")
            }
        }

        // Clear the original collection after removal attempts
        overlays.clear()
        visibleLinks.clear()
        currentLinkPositions.clear()

        // Log completion
        Log.d(TAG, "All overlays removed")
    }

    private fun triggerVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    private fun showAccuracyNotification(
        accuracy: Double,
        precision: Double,
        recall: Double,
        f1Score: Double,
        tp: Int,
        tn: Int,
        fp: Int,
        fn: Int
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        // Create an expandable notification with accuracy details
        val inboxStyle = NotificationCompat.InboxStyle()
        inboxStyle.setBigContentTitle("Scanner Accuracy")

        inboxStyle.addLine("Accuracy: ${accuracy.toInt()}%")
        inboxStyle.addLine("Precision: ${precision.toInt()}%")
        inboxStyle.addLine("Recall: ${recall.toInt()}%")
        inboxStyle.addLine("F1 Score: ${f1Score.toInt()}")
        inboxStyle.addLine("TP: $tp, TN: $tn, FP: $fp, FN: $fn")

        notificationBuilder
            .setContentTitle("Scanner Performance")
            .setContentText("View accuracy metrics")
            .setStyle(inboxStyle)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun showNotification(title: String, content: String, maliciousLinks: List<ScanResult>? = null) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use the same notification channel and ID for both foreground service and malicious link notifications
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)

        if (maliciousLinks != null && maliciousLinks.isNotEmpty()) {
            val linkCount = maliciousLinks.size

            // Create an expandable notification with detailed link information
            val inboxStyle = NotificationCompat.InboxStyle()
            inboxStyle.setBigContentTitle("⚠️ Phishing Links Detected")

            maliciousLinks.forEach { link ->
                inboxStyle.addLine("${link.url} - ${link.description}")
            }

            notificationBuilder
                .setContentTitle("⚠️ Phishing Links Detected")
                .setContentText(
                    if (linkCount == 1) "1 malicious link detected"
                    else "$linkCount malicious links detected"
                )
                .setStyle(inboxStyle)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setNumber(linkCount)
        } else {
            // Use the original foreground service notification
            notificationBuilder
                .setContentTitle("Screen Capture Active")
                .setContentText("Monitoring screen for potential threats")
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        // Update the existing foreground notification or create a new one
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    fun forceRemoveAllOverlays() {
        isPaused.set(true)
        Handler(Looper.getMainLooper()).post {
            removeAllOverlays()
        }
    }

    private fun resetMetrics() {
        truePositives = 0
        trueNegatives = 0
        falsePositives = 0
        falseNegatives = 0
        Log.d(TAG, "Metrics reset")
    }

    private fun stopScreenCapture() {
        try {
            isScanning.set(false)
            scanningScope.cancel()

            mediaProjection?.let {
                it.unregisterCallback(mediaProjectionCallback)
                it.stop()
                mediaProjection = null
            }

            virtualDisplay?.release()
            virtualDisplay = null

            removeAllOverlays()
            visibleLinks.clear()
            currentLinkPositions.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping screen capture", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service is being destroyed")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(scanControlReceiver)
        stopScreenCapture()
        removeAllOverlays()
        resetMetrics()
        super.onDestroy()
    }

    data class ScanResult(
        val url: String,
        val isMalicious: Boolean,
        val description: String
    )
}