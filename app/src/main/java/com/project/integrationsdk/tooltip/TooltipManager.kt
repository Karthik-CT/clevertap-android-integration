package com.project.integrationsdk.tooltip

import android.content.Context
import android.view.View

class TooltipManager(private val context: Context) {

    data class TooltipConfig(
        val anchorView: View,
        val text: String,
        val gravity: Tooltip.Gravity,
        val textColor: Int,
        val backgroundColor: String,
        val maxWidth: Int = 0 // Optional default, can be customized per tooltip if needed
    )

    private var currentTooltipIndex = 0
    private lateinit var tooltips: List<TooltipConfig>

    fun showTooltipsSequentially(tooltips: List<TooltipConfig>) {
        this.tooltips = tooltips
        currentTooltipIndex = 0
        showNextTooltip()
    }

    private fun showNextTooltip() {
        if (currentTooltipIndex >= tooltips.size) return

        val config = tooltips[currentTooltipIndex]
        Tooltip.Builder(context)
            .anchor(config.anchorView, 0, 0, false)
            .text(config.text)
            .tooltipTextColor(config.textColor)
            .tooltipBackgroundColor(config.backgroundColor)
            .maxWidth(config.maxWidth)
            .arrow(true)
            .closePolicy(getClosePolicy())
            .showDuration(10000)
            .overlay(true)
            .create()
            .doOnHidden {
                currentTooltipIndex++
                showNextTooltip() // Show the next tooltip after current one is dismissed
            }
            .show(config.anchorView, config.gravity, true)
    }

    private fun getClosePolicy(): ClosePolicy {
        val builder = ClosePolicy.Builder()
        builder.inside(true)
        builder.outside(true)
        builder.consume(true)
        return builder.build()
    }
}
