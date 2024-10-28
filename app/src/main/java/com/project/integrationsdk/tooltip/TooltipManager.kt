package com.project.integrationsdk.tooltip

//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Color
//import android.view.View
//import androidx.appcompat.app.AppCompatActivity
//import com.project.integrationsdk.R
//import com.project.integrationsdk.TooltipsActivity
//import org.json.JSONException
//import org.json.JSONObject
//
//class TooltipManager(private val context: Context) {
//
//    fun renderTooltipsFromJson(jsonObject: JSONObject) {
//        val tooltips = mutableListOf<TooltipConfig>()
//
//        try {
//            jsonObject.keys().forEach { key ->
//                if (key.startsWith("nd_title") && jsonObject.has("${key}_id")) {
//                    val title = jsonObject.getString(key)
//                    val idName = jsonObject.getString("${key}_id")
//                    val viewId = context.resources.getIdentifier(idName, "id", context.packageName)
//                    val view = (context as AppCompatActivity).findViewById<View>(viewId)
//
//                    // Decide tooltip gravity based on key (e.g., could also read from JSON if needed)
//                    val gravity = when (key) {
//                        "nd_title1" -> Tooltip.Gravity.BOTTOM
//                        "nd_title2" -> Tooltip.Gravity.LEFT
//                        "nd_title3" -> Tooltip.Gravity.RIGHT
//                        else -> Tooltip.Gravity.TOP
//                    }
//
//                    // Add to list if view and title are valid
//                    if (view != null) {
//                        tooltips.add(TooltipConfig(view, title, gravity))
//                    }
//                }
//            }
//        } catch (e: JSONException) {
//            e.printStackTrace()
//        }
//
//        // Display the tooltips sequentially
//        showTooltipsSequentially(tooltips)
//    }
//
//    fun showTooltipsSequentially(tooltips: List<TooltipConfig>) {
//        showTooltipsSequentially(tooltips, 0)
//    }
//
//    @SuppressLint("ResourceType")
//    private fun showTooltipsSequentially(tooltips: List<TooltipConfig>, index: Int) {
//        if (index >= tooltips.size) return
//
//        val config = tooltips[index]
//
//        Tooltip.Builder(context)
//            .anchor(config.anchorView, 0, 0, false)
//            .text(config.text)
//            .tooltipTextColor(Color.parseColor("#FFFFFF"))
//            .tooltipBackgroundColor("#FF0000")
//            .styleId(R.style.ToolTipAltStyle)
//            .maxWidth((context.resources.displayMetrics.widthPixels / 2))
//            .arrow(true)
//            .closePolicy(getClosePolicy())
//            .showDuration(10000)
//            .overlay(true)
//            .create()
//            .doOnHidden {
//                showTooltipsSequentially(tooltips, index + 1)
//            }
//            .show(config.anchorView, config.gravity, true)
//    }
//
//    data class TooltipConfig(
//        val anchorView: View,
//        val text: String,
//        val gravity: Tooltip.Gravity = Tooltip.Gravity.TOP,
//    )
//
//    private fun getClosePolicy(): ClosePolicy {
//        val builder = ClosePolicy.Builder()
//        builder.inside(true)
//        builder.outside(true)
//        builder.consume(true)
//        return builder.build()
//    }
//}

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

    /**
     * Show tooltips sequentially based on the provided list of TooltipConfig.
     */
    fun showTooltipsSequentially(tooltips: List<TooltipConfig>) {
        this.tooltips = tooltips
        currentTooltipIndex = 0
        showNextTooltip()
    }

    /**
     * Display the tooltip at the current index and set a callback to show the next one when dismissed.
     */
    private fun showNextTooltip() {
        if (currentTooltipIndex >= tooltips.size) return // No more tooltips to show

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
