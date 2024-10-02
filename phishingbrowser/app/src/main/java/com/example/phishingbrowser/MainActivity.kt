package com.example.phishingbrowser

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var backButton: Button
    private lateinit var forwardButton: Button
    private lateinit var tabsButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var webviewContainer: FrameLayout  // Correct the type here

    private val tabList = mutableListOf<WebView>()  // Store multiple WebViews
    private val tabTitles = mutableListOf<String>()  // Store the titles of each tab
    private var currentTab = 0  // Track the current active tab

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        urlInput = findViewById(R.id.urlInput)
        backButton = findViewById(R.id.backButton)
        forwardButton = findViewById(R.id.forwardButton)
        tabsButton = findViewById(R.id.tabsButton)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        webviewContainer = findViewById(R.id.webviewContainer)  // No more casting error

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            getCurrentWebView().reload()
        }

        // Add an initial tab
        addNewTab()

        // Tabs button functionality
        tabsButton.setOnClickListener {
            showTabsDialog()
        }

        // Back button functionality
        backButton.setOnClickListener {
            if (getCurrentWebView().canGoBack()) {
                getCurrentWebView().goBack()
            }
        }

        // Forward button functionality
        forwardButton.setOnClickListener {
            if (getCurrentWebView().canGoForward()) {
                getCurrentWebView().goForward()
            }
        }

        // URL Input handling
        urlInput.setOnEditorActionListener { _, _, _ ->
            val url = formatUrl(urlInput.text.toString())
            getCurrentWebView().loadUrl(url)
            true
        }
    }

    // Add a new tab with a WebView
    private fun addNewTab() {
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
                urlInput.setText(url)

                // Update the tab title
                val title = view?.title ?: "New Tab"
                if (tabList.size > currentTab) {
                    tabTitles[currentTab] = title
                }
            }
        }

        webView.loadUrl("https://www.google.com")
        tabList.add(webView)
        tabTitles.add("New Tab")  // Add a default title

        switchToTab(tabList.size - 1)  // Switch to the newly created tab
    }

    // Switch to a specific tab
    private fun switchToTab(tabIndex: Int) {
        webviewContainer.removeAllViews()  // Clear the container
        webviewContainer.addView(tabList[tabIndex])  // Add the WebView of the selected tab
        currentTab = tabIndex
        urlInput.setText(tabList[tabIndex].url)
    }

    // Get the current active WebView
    private fun getCurrentWebView(): WebView {
        return tabList[currentTab]
    }

    // Show the list of open tabs and allow closing tabs
    private fun showTabsDialog() {
        val tabTitlesArray = tabTitles.toTypedArray()  // Get the titles of the open tabs

        AlertDialog.Builder(this)
            .setTitle("Open Tabs")
            .setItems(tabTitlesArray) { _, which ->
                switchToTab(which)  // Switch to the selected tab
            }
            .setPositiveButton("New Tab") { _, _ ->
                addNewTab()  // Add a new tab
            }
            .setNegativeButton("Close Tab") { _, _ ->
                closeCurrentTab()  // Close the currently active tab
            }
            .show()
    }

    // Close the current tab
    private fun closeCurrentTab() {
        if (tabList.size > 1) {
            tabList.removeAt(currentTab)
            tabTitles.removeAt(currentTab)
            if (currentTab >= tabList.size) {
                currentTab = tabList.size - 1
            }
            switchToTab(currentTab)
        }
    }

    // Format URL (check if it's a search term or URL)
    private fun formatUrl(input: String): String {
        return if (input.contains(".")) {
            if (!input.startsWith("http://") && !input.startsWith("https://")) {
                "https://$input"
            } else {
                input
            }
        } else {
            "https://www.google.com/search?q=$input"
        }
    }

    // Handle the back button press
    override fun onBackPressed() {
        if (getCurrentWebView().canGoBack()) {
            getCurrentWebView().goBack()
        } else {
            super.onBackPressed()
        }
    }
}
