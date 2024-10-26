package com.project.integrationsdk.tooltip

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import com.project.integrationsdk.R

class TooltipManager(private val context: Context) {

    // Function to show tooltips one by one
    fun showTooltipsSequentially(tooltips: List<TooltipConfig>) {
        showTooltipsSequentially(tooltips, 0)
    }

    @SuppressLint("ResourceType")
    private fun showTooltipsSequentially(tooltips: List<TooltipConfig>, index: Int) {
        if (index >= tooltips.size) return // No more tooltips to show

        val config = tooltips[index]

        Tooltip.Builder(context)
            .anchor(config.anchorView, 0, 0, false)
            .text(config.text)
            .styleId(R.style.ToolTipAltStyle)
            .maxWidth((context.resources.displayMetrics.widthPixels / 2))
            .arrow(true)
            .closePolicy(getClosePolicy())
            .showDuration(10000)
            .overlay(true)
            .create()
            .doOnHidden {
                // Show the next tooltip after the current one is hidden
                showTooltipsSequentially(tooltips, index + 1)
            }
            .show(config.anchorView, config.gravity, true)
    }

    // Data class to represent tooltip configuration
    data class TooltipConfig(
        val anchorView: View,
        val text: String,
        val gravity: Tooltip.Gravity,
        val backgroundColor: Int
    )

    private fun getClosePolicy(): ClosePolicy {
        val builder = ClosePolicy.Builder()
        builder.inside(true)
        builder.outside(true)
        builder.consume(true)
        return builder.build()
    }
}
