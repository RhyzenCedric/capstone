package com.example.sharedlibrary

import android.webkit.WebView

object BrowserUtils {
    private var currentWebView: WebView? = null

    fun setCurrentWebView(webView: WebView) {
        currentWebView = webView
    }

    fun getCurrentWebView(): WebView {
        return currentWebView ?: throw IllegalStateException("No active WebView.")
    }
}