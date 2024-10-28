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