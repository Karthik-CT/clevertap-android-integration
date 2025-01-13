package com.project.integrationsdk.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class CurvedView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFD75A6B.toInt() // Red color (use your hex color)
        style = Paint.Style.FILL
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        // Draw the smooth curve
        path.reset()
        path.moveTo(0f, 0f) // Top-left corner
        path.lineTo(0f, height * 0.8f) // Left side depth point
        path.quadTo(
            width / 2, height, // Curve's control point
            width, height * 0.8f // Right side depth point
        )
        path.lineTo(width, 0f) // Top-right corner
        path.close()

        canvas.drawPath(path, paint)
    }
}
