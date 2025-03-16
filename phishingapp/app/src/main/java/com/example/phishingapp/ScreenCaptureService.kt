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
import android.util.Patterns
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
    private val scanningScope = CoroutineScope(Dispatchers.Default)
    private val isScanning = AtomicBoolean(false)

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
        // Remove the separate notification creation
        // The showNotification method will handle the initial foreground notification
        showNotification("Screen Capture Active", "Monitoring screen for potential threats")
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
                            val scanResults = performImageScanning(bitmap)

                            withContext(Dispatchers.Main) {
                                handleScanResult(scanResults)
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

    private fun performImageScanning(bitmap: Bitmap): List<ScanResult> {
        Log.d(TAG, "performImageScanning: performing link scanning")

        try {
            val extractedText = simulateOCR(bitmap)
            val urls = extractUrls(extractedText)

            return urls.map { urlString ->
                val scanResult = scanUrl(urlString)
                Log.d(TAG, "Scanned URL: $urlString - ${scanResult.description}")
                scanResult
            }
        } catch (e: Exception) {
            Log.e(TAG, "URL scanning error", e)
            return listOf(
                ScanResult(
                    url = "Unknown",
                    isMalicious = false,
                    description = "Scanning error occurred"
                )
            )
        }
    }

    private fun simulateOCR(bitmap: Bitmap): String {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        return try {
            val result = Tasks.await(recognizer.process(image))
            val extractedText = result.textBlocks.joinToString("\n") { block ->
                block.text
            }

            Log.d(TAG, "OCR extracted text: $extractedText")
            extractedText
        } catch (e: ExecutionException) {
            Log.e(TAG, "ML Kit OCR execution error", e)
            ""
        } catch (e: InterruptedException) {
            Log.e(TAG, "ML Kit OCR interrupted", e)
            ""
        } catch (e: IOException) {
            Log.e(TAG, "ML Kit OCR IO error", e)
            ""
        } finally {
            recognizer.close()
        }
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
                "bank.com"
            )

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
                it.length > 100 ||                             // Very long URL
                        (it.count { char -> char == '.' } > 3) ||      // Too many subdomains
                        (it.count { char -> char == '/' } > 4) ||      // Too many path segments
                        (it.count { char -> char == '-' } > 2)         // Multiple hyphens
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

    private fun handleScanResult(scanResults: List<ScanResult>) {
        var maliciousLinksDetected = false
        val maliciousLinks = mutableListOf<String>() // Store detected malicious links

        scanResults.forEach { result ->
            if (result.isMalicious) {
                maliciousLinksDetected = true
                maliciousLinks.add(result.url)
                Log.d(TAG, "Malicious Link Detected: ${result.url} - ${result.description}")
            }
        }

        if (maliciousLinksDetected) {
            val intent = Intent("com.example.phishingapp.MALICIOUS_LINK_DETECTED")
            intent.putExtra("maliciousLink", maliciousLinks.first()) // Pass the first detected malicious link
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            showNotification(
                "⚠️ Phishing Link Detected",
                "Malicious link: ${maliciousLinks.first()}",
                scanResults
            )
        } else {
            LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent("com.example.phishingapp.NO_MALICIOUS_LINKS"))
        }
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
                .setOngoing(true) // Makes the notification ongoing (persistent)
                .setAutoCancel(false) // Prevents it from being dismissed by swiping
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        }

        // Update the existing foreground notification or create a new one
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopScreenCapture() {
        try {
            isScanning.set(false)
            scanningScope.cancel()

            mediaProjection?.let {
                it.unregisterCallback(mediaProjectionCallback)
                it.stop()
            }

            virtualDisplay?.release()
            virtualDisplay = null

            mediaProjection = null

            Log.d(TAG, "Screen capture stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping screen capture", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service is being destroyed")
        stopScreenCapture()
        super.onDestroy()
    }

    data class ScanResult(
        val url: String,
        val isMalicious: Boolean,
        val description: String
    )
}