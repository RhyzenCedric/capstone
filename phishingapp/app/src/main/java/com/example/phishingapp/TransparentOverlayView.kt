package com.example.phishingapp

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View

class TransparentOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        // For debugging, make the overlay visible
        setBackgroundColor(Color.argb(128, 255, 0, 0)) // Semi-transparent red background
        // In production, use setBackgroundColor(Color.TRANSPARENT)
    }
}