package com.project.integrationsdk.spotlight.shape


import android.animation.TimeInterpolator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF

interface Shape {
    val duration: Long
    val interpolator: TimeInterpolator
    fun draw(canvas: Canvas, point: PointF, value: Float, paint: Paint)
}