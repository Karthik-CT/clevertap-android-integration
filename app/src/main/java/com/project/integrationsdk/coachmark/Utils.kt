package com.project.integrationsdk.coachmark


import android.content.Context
import android.util.TypedValue
import android.view.View
import com.project.integrationsdk.databinding.CoachmarkitemCoachmarkBinding

object Utils {

    fun dpToPx(context: Context, dp: Int): Float {
        return dpToPx(context, dp.toFloat())
    }

    private fun dpToPx(context: Context, dipValue: Float): Float {
        val metrics = context.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
    }
}

interface OverlayClickListener {
    fun onOverlayClick(overlay: CoachMarkOverlay, binding: CoachmarkitemCoachmarkBinding)
}

interface SkipClickListener {
    fun onSkipClick(view: View)
}

enum class Shape {
    BOX
}

enum class GravityIn {
    CENTER,
    START,
    END,
    TOP,
    BOTTOM
}

enum class Gravity {
    START_TOP,
    END_TOP,
    START_BOTTOM,
    END_BOTTOM,
    TOP,
    BOTTOM,
    NULL
}