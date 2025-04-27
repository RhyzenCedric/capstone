package com.example.phishingapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class SonarWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {
    private val wavePaint = Paint().apply {
        color = 0xFF1D1E4E.toInt() // Dark blue color for stroke
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val wavePath = Path()
    private val waves = mutableListOf<Wave>()
    private var animator: ValueAnimator? = null
    private val waveDuration = 3000L // 3 seconds per wave
    private val waveInterval = 1000L // New wave every 1.5 seconds

    // The fill color we want to use
    private val fillColor = Color.parseColor("#011dcb")

    // Animation state control
    private var isAnimationEnabled = true

    // Static wave positions for still image (when animation is off)
    private val staticWavePositions = listOf(0.2f, 0.5f, 0.8f)

    init {
        startAnimation()
    }

    /**
     * Toggle animation on or off
     * @param enabled True to enable animation, false to show static waves
     */
    fun setAnimationEnabled(enabled: Boolean) {
        if (isAnimationEnabled == enabled) return

        isAnimationEnabled = enabled

        if (isAnimationEnabled) {
            startAnimation()
        } else {
            stopAnimation()
            invalidate() // Redraw with static waves
        }
    }

    /**
     * Check if animation is currently enabled
     * @return True if animation is running, false otherwise
     */
    fun isAnimationEnabled(): Boolean {
        return isAnimationEnabled
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAnimationEnabled) {
            drawAnimatedWaves(canvas)
        } else {
            drawStaticWaves(canvas)
        }
    }

    private fun drawAnimatedWaves(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = (Math.hypot(width.toDouble(), height.toDouble()) / 2f).toFloat()

        val iterator = waves.iterator()
        while (iterator.hasNext()) {
            val wave = iterator.next()
            drawWave(canvas, centerX, centerY, maxRadius, wave.progress, (255 * (1 - wave.progress)).toInt())

            if (wave.progress >= 1f) {
                iterator.remove()
            }
        }
    }

    private fun drawStaticWaves(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = (Math.hypot(width.toDouble(), height.toDouble()) / 2f).toFloat()

        // Draw three static waves at different positions
        for (position in staticWavePositions) {
            // Calculate alpha based on position (farther waves are more transparent)
            val alpha = (255 * (1 - position) * 0.8).toInt()
            drawWave(canvas, centerX, centerY, maxRadius, position, alpha)
        }
    }

    private fun drawWave(canvas: Canvas, centerX: Float, centerY: Float, maxRadius: Float, progress: Float, alpha: Int) {
        val radius = maxRadius * progress

        // Skip drawing if radius is too small
        if (radius < 1f) {
            return
        }

        // Configure stroke (outline) of wave
        wavePaint.alpha = alpha

        // Draw arc for the wave outline
        val left = centerX - radius
        val top = centerY - radius
        val right = centerX + radius
        val bottom = centerY + radius

        // Create colors for the gradient - solid color at the arc, transparent at center
        val solidColor = Color.argb(
            alpha,
            Color.red(fillColor),
            Color.green(fillColor),
            Color.blue(fillColor)
        )
        val transparentColor = Color.argb(
            0,
            Color.red(fillColor),
            Color.green(fillColor),
            Color.blue(fillColor)
        )

        // Draw the filled area with a linear gradient from arc to center
        wavePath.reset()
        wavePath.moveTo(centerX - radius, centerY)
        wavePath.arcTo(left, top, right, bottom, 180f, 180f, false)
        wavePath.lineTo(centerX, centerY)
        wavePath.close()

        // Create a linear gradient for each arc - solid at arc edge, transparent at center
        fillPaint.shader = LinearGradient(
            centerX, centerY - radius, // Start point (top of arc)
            centerX, centerY,          // End point (center)
            solidColor,                // Solid color at the arc
            transparentColor,          // Transparent at center
            Shader.TileMode.CLAMP
        )

        canvas.drawPath(wavePath, fillPaint)

        // Draw the outline after the fill
        canvas.drawArc(
            left, top, right, bottom,
            180f, 180f, // startAngle, sweepAngle
            false, wavePaint
        )
    }

    private fun startAnimation() {
        if (animator != null) {
            return // Animation already running
        }

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = waveDuration
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                val time = System.currentTimeMillis()
                if (waves.isEmpty() || time - (waves.lastOrNull()?.createdAt ?: 0) >= waveInterval) {
                    waves.add(Wave(time))
                }
                for (wave in waves) {
                    wave.update(time, waveDuration)
                }
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimation() {
        animator?.cancel()
        animator = null
        waves.clear()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    private class Wave(val createdAt: Long) {
        var progress: Float = 0f
            private set

        fun update(currentTime: Long, duration: Long) {
            progress = ((currentTime - createdAt).toFloat() / duration).coerceIn(0f, 1f)
        }
    }
}