package com.example.phishingbrowser

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var backButton: Button
    private lateinit var forwardButton: Button
    private lateinit var tabsButton: Button
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var webviewContainer: FrameLayout
    private lateinit var tabRecyclerView: RecyclerView  // For showing tabs list

    private val tabList = mutableListOf<WebView>()  // List of WebViews (tabs)
    private val tabTitles = mutableListOf<String>()  // List of tab titles
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
        webviewContainer = findViewById(R.id.webviewContainer)
        tabRecyclerView = findViewById(R.id.tabRecyclerView)

        // Set up RecyclerView for tabs
        val adapter = TabAdapter(this, tabTitles, { switchToTab(it) }, { closeTab(it) }, { addNewTab() })
        tabRecyclerView.layoutManager = LinearLayoutManager(this)
        tabRecyclerView.adapter = adapter

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            getCurrentWebView().reload()
        }

        // Add an initial tab
        addNewTab()

        // Tabs button functionality
        tabsButton.setOnClickListener {
            toggleTabView()
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

    private fun toggleTabView() {
        if (tabRecyclerView.visibility == View.GONE) {
            tabRecyclerView.visibility = View.VISIBLE
            swipeRefreshLayout.visibility = View.GONE
        } else {
            tabRecyclerView.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
        }
    }

    private fun addNewTab() {
        val webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
                // Set the URL input to be blank
                urlInput.setText("")

                // Update the tab title in real-time
                val title = view?.title ?: "New Tab"
                if (tabList.size > currentTab) {
                    tabTitles[currentTab] = title
                }
                // Notify the adapter to update the tab title
                (tabRecyclerView.adapter as TabAdapter).notifyItemChanged(currentTab)
            }
        }

        // Load Google as the initial page but don't show it in the input field
        webView.loadUrl("https://www.google.com")
        tabList.add(webView)
        tabTitles.add("New Tab")  // Add default title
        switchToTab(tabList.size - 1)  // Switch to the newly created tab
    }


    private fun closeTab(position: Int) {
        // Remove the tab at the specified position
        tabList.removeAt(position)
        tabTitles.removeAt(position)
        // Notify the adapter that the item has been removed
        (tabRecyclerView.adapter as TabAdapter).notifyItemRemoved(position)

        // Check if there are no tabs left
        if (tabList.isEmpty()) {
            addNewTab()  // Ensure at least one tab is always open
        } else {
            // If the closed tab was the current one, switch to the first tab
            if (currentTab == position) {
                currentTab = if (position < tabList.size) position else position - 1
                switchToTab(currentTab)
            } else {
                // Switch to the previous tab if the current tab is not closed
                switchToTab(if (currentTab > position) currentTab - 1 else currentTab)
            }
        }
    }

    private fun switchToTab(position: Int) {
        // Check if the position is valid
        if (position >= 0 && position < tabList.size) {
            currentTab = position
            webviewContainer.removeAllViews()
            webviewContainer.addView(tabList[position])
            tabRecyclerView.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE

            // Update the input field with the URL of the current tab
            urlInput.setText(tabList[position].url)  // Set the input field to the current tab's URL
            tabTitles[position] = tabList[position].title ?: "Untitled"  // Update tab title
            // Notify the adapter to update the tab title
            (tabRecyclerView.adapter as TabAdapter).notifyItemChanged(position)
        }
    }

    private fun getCurrentWebView(): WebView {
        return tabList[currentTab]
    }

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

    override fun onBackPressed() {
        if (getCurrentWebView().canGoBack()) {
            getCurrentWebView().goBack()
        } else {
            super.onBackPressed()
        }
    }
}
