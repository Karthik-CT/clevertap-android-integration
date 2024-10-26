package com.project.integrationsdk.tooltip

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import com.project.integrationsdk.R
import org.json.JSONObject

class TooltipHelper {

     fun showTooltips(anyActivity: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        val tooltipCount = unit.getJSONObject("custom_kv").getInt("nd_tooltipCount")
        val textColor = unit.getJSONObject("custom_kv").getString("nd_textColor")
        val backgroundColor = unit.getJSONObject("custom_kv").getString("nd_backgroundColor")

        fun showNextTooltip(index: Int) {
            if (index < tooltipCount) {
                val viewIdName = unit.getJSONObject("custom_kv").getString("nd_title${index + 1}_id")
                val viewId = anyActivity.resources.getIdentifier(viewIdName, "id", anyActivity.packageName)
                val text = unit.getJSONObject("custom_kv").getString("nd_title${index + 1}")
                val customKv = unit.optJSONObject("custom_kv") ?: return

                fun getGravity(index: Int): Tooltip.Gravity {
                    return when (customKv.optString("nd_title${index + 1}_gravity", "TOP").uppercase()) {
                        "RIGHT" -> Tooltip.Gravity.RIGHT
                        "LEFT" -> Tooltip.Gravity.LEFT
                        "BOTTOM" -> Tooltip.Gravity.BOTTOM
                        "TOP" -> Tooltip.Gravity.TOP
                        else -> Tooltip.Gravity.TOP
                    }
                }

                Tooltip.Builder(anyActivity)
                    .anchor(anyActivity.findViewById(viewId), 0, 0, false)
                    .text(text)
                    .tooltipTextColor(Color.parseColor(textColor))
                    .tooltipBackgroundColor(backgroundColor)
                    .styleId(R.style.ToolTipAltStyle)
                    .maxWidth(anyActivity.resources.displayMetrics.widthPixels / 2)
                    .arrow(true)
                    .closePolicy(getClosePolicy())
                    .showDuration(10000)
                    .overlay(true)
                    .create()
                    .doOnHidden { showNextTooltip(index + 1) }
                    .show(anyActivity.findViewById(viewId), getGravity(index), true)
            }
        }

        showNextTooltip(0)

         onComplete()

    }

    private fun getClosePolicy(): ClosePolicy {
        val builder = ClosePolicy.Builder()
        builder.inside(true)
        builder.outside(true)
        builder.consume(true)
        return builder.build()
    }
}
