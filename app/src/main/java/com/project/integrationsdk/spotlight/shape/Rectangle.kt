package com.project.integrationsdk.spotlight.shape

import android.animation.TimeInterpolator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.view.animation.DecelerateInterpolator
import java.util.concurrent.TimeUnit

class Rectangle @JvmOverloads constructor(
    private val width: Float,
    private val height: Float,
    override val duration: Long = DEFAULT_DURATION,
    override val interpolator: TimeInterpolator = DEFAULT_INTERPOLATOR
) : Shape {

    override fun draw(canvas: Canvas, point: PointF, value: Float, paint: Paint) {
        val halfWidth = (value * width) / 2
        val halfHeight = (value * height) / 2
        val rect = RectF(
            point.x - halfWidth, // Left
            point.y - halfHeight, // Top
            point.x + halfWidth, // Right
            point.y + halfHeight // Bottom
        )
        canvas.drawRect(rect, paint)
    }

    companion object {

        val DEFAULT_DURATION = TimeUnit.MILLISECONDS.toMillis(500)

        val DEFAULT_INTERPOLATOR = DecelerateInterpolator(2f)
    }
}
