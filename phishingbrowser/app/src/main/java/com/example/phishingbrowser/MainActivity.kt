package com.example.phishingbrowser

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var backButton: Button
    private lateinit var forwardButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize WebView and UI elements
        webView = findViewById(R.id.webview)
        urlInput = findViewById(R.id.urlInput)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // Enable JavaScript
        webView.settings.javaScriptEnabled = true

        // Ensure links open in WebView, not the default browser
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Update the EditText with the current URL
                urlInput.setText(url)
                swipeRefreshLayout.isRefreshing = false // Stop refreshing indicator
            }
        }

        // Load a default URL
        webView.loadUrl("https://www.google.com")
        urlInput.setText("https://www.google.com")

        // Handle the input field for URL entry
        urlInput.setOnEditorActionListener { _, _, _ ->
            val input = urlInput.text.toString().trim() // Trim whitespace from input
            val formattedUrl = if (input.isNotEmpty()) {
                // Check if input contains a dot, indicating a potential domain
                if (input.contains(".")) {
                    // If it looks like a domain, prepend "https://"
                    "https://www.$input"
                } else if (input.contains(" ") || input.split(" ").size > 2) {
                    // If input is a search term (contains spaces) or more than two words, redirect to Google search
                    "https://www.google.com/search?q=$input"
                } else {
                    // Treat single words as search terms or append 'com' if it looks like a name
                    if (input.length > 0) {
                        "https://www.google.com/search?q=$input"
                    } else {
                        null
                    }
                }
            } else {
                null
            }

            // Load the URL in WebView if formattedUrl is not null
            formattedUrl?.let { webView.loadUrl(it) }
            true // Return true to indicate the action was handled
        }
        // Back button functionality
        backButton.setOnClickListener {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }

        // Forward button functionality
        forwardButton.setOnClickListener {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }

        // Set up SwipeRefreshLayout to refresh the WebView
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }
    }

    // Handle the back button for WebView navigation
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
