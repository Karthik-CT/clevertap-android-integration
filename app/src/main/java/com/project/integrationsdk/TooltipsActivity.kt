package com.project.integrationsdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.databinding.ActivityTooltipsBinding
import com.project.integrationsdk.tooltip.Tooltip
import com.project.integrationsdk.tooltip.TooltipHelper
import com.project.integrationsdk.tooltip.TooltipManager
import java.util.ArrayList

class TooltipsActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityTooltipsBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    var tooltip: Tooltip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTooltipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        cleverTapDefaultInstance?.pushEvent("ToolsTips Event")

        cleverTapDefaultInstance?.setDisplayUnitListener(this)
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        println("ToolTips unit: $unit")
        unit.customExtras.forEach { (key, value) ->
            println("$key: $value")
        }

        if (unit.customExtras["nd_id"] == "nd_tooltips") {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            TooltipHelper().showTooltips(this@TooltipsActivity, unit.jsonObject){
                CleverTapAPI.getDefaultInstance(this@TooltipsActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }
}


//            val viewId1 = resources.getIdentifier(unit.customExtras["nd_title1_id"], "id", packageName)
//            val viewId2 = resources.getIdentifier(unit.customExtras["nd_title2_id"], "id", packageName)
//            val viewId3 = resources.getIdentifier(unit.customExtras["nd_title3_id"], "id", packageName)
//            val viewId4 = resources.getIdentifier(unit.customExtras["nd_title4_id"], "id", packageName)
//            Tooltip.Builder(applicationContext)
//                .anchor(findViewById(viewId1), 0, 0, false)
//                .text(unit.customExtras["nd_title1"].toString())
//                .tooltipTextColor(Color.parseColor(unit.customExtras["nd_textColor"].toString()))
//                .tooltipBackgroundColor(unit.customExtras["nd_backgroundColor"].toString())
//                .styleId(R.style.ToolTipAltStyle)
//                .maxWidth(resources.displayMetrics.widthPixels / 2)
//                .arrow(true)
//                .closePolicy(getClosePolicy())
//                .showDuration(10000)
//                .overlay(true)
//                .create()
//                .doOnHidden {
//                    // Show the second tooltip when the first is dismissed
//                    Tooltip.Builder(applicationContext)
//                        .anchor(findViewById(viewId2), 0, 0, false)
//                        .text(unit.customExtras["nd_title2"].toString())
//                        .tooltipTextColor(Color.parseColor(unit.customExtras["nd_textColor"].toString()))
//                        .tooltipBackgroundColor(unit.customExtras["nd_backgroundColor"].toString())
//                        .styleId(R.style.ToolTipAltStyle)
//                        .maxWidth(resources.displayMetrics.widthPixels / 2)
//                        .arrow(true)
//                        .closePolicy(getClosePolicy())
//                        .showDuration(10000)
//                        .overlay(true)
//                        .create()
//                        .doOnHidden {
//                            // Show the third tooltip when the second is dismissed
//                            Tooltip.Builder(applicationContext)
//                                .anchor(findViewById(viewId3), 0, 0, false)
//                                .text(unit.customExtras["nd_title3"].toString())
//                                .tooltipTextColor(Color.parseColor(unit.customExtras["nd_textColor"].toString()))
//                                .tooltipBackgroundColor(unit.customExtras["nd_backgroundColor"].toString())
//                                .styleId(R.style.ToolTipAltStyle)
//                                .maxWidth(resources.displayMetrics.widthPixels / 2)
//                                .arrow(true)
//                                .closePolicy(getClosePolicy())
//                                .showDuration(10000)
//                                .overlay(true)
//                                .create()
//                                .doOnHidden {
//                                    // Show the fourth tooltip when the third is dismissed
//                                    Tooltip.Builder(applicationContext)
//                                        .anchor(findViewById(viewId4), 0, 0, false)
//                                        .text(unit.customExtras["nd_title4"].toString())
//                                        .tooltipTextColor(Color.parseColor(unit.customExtras["nd_textColor"].toString()))
//                                        .tooltipBackgroundColor(unit.customExtras["nd_backgroundColor"].toString())
//                                        .styleId(R.style.ToolTipAltStyle)
//                                        .maxWidth(resources.displayMetrics.widthPixels / 2)
//                                        .arrow(true)
//                                        .closePolicy(getClosePolicy())
//                                        .showDuration(10000)
//                                        .overlay(true)
//                                        .create()
//                                        .show(binding.TextView4, Tooltip.Gravity.TOP, true)
//                                }
//                                .show(binding.TextView3, Tooltip.Gravity.RIGHT, true)
//                        }
//                        .show(binding.TextView2, Tooltip.Gravity.LEFT, true)
//                }
//                .show(binding.TextView1, Tooltip.Gravity.BOTTOM, true)


//            val viewId1 = resources.getIdentifier(unit.customExtras["nd_title1_id"], "id", packageName)
//            val viewId2 = resources.getIdentifier(unit.customExtras["nd_title2_id"], "id", packageName)
//            val viewId3 = resources.getIdentifier(unit.customExtras["nd_title3_id"], "id", packageName)
//            val viewId4 = resources.getIdentifier(unit.customExtras["nd_title4_id"], "id", packageName)
//            tooltipManager = TooltipManager(applicationContext)
//            val tooltips = listOf(
//                TooltipManager.TooltipConfig(findViewById(viewId1), unit.customExtras["nd_title1"]!!, Tooltip.Gravity.BOTTOM),
//                TooltipManager.TooltipConfig(findViewById(viewId2), unit.customExtras["nd_title2"]!!, Tooltip.Gravity.LEFT),
//                TooltipManager.TooltipConfig(findViewById(viewId3), unit.customExtras["nd_title3"]!!, Tooltip.Gravity.RIGHT),
//                TooltipManager.TooltipConfig(findViewById(viewId4), unit.customExtras["nd_title4"]!!, Tooltip.Gravity.TOP)
//            )
//            tooltipManager.showTooltipsSequentially(tooltips)