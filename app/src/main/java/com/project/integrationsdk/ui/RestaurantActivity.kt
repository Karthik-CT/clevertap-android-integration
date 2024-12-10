package com.project.integrationsdk.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.coachmark.CoachMarkHelper
import com.project.integrationsdk.coachmark.CoachMarkSequence
import com.project.integrationsdk.databinding.ActivityRestaurantBinding
import java.util.ArrayList

class RestaurantActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityRestaurantBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    lateinit var coachMarkSequence: CoachMarkSequence

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        coachMarkSequence = CoachMarkSequence(this)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)

        cleverTapDefaultInstance?.pushEvent("Karthik's Native Display Event")
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        unit.customExtras.forEach { (key, value) ->
            println("$key: $value")
        }
        if (unit.customExtras["nd_id"] == "nd_coachmarks") {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            CoachMarkHelper().renderCoachMark(this, unit.jsonObject){
                CleverTapAPI.getDefaultInstance(this@RestaurantActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }
}

