package com.project.integrationsdk.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.databinding.ActivityRestaurantBinding
import org.json.JSONObject
import java.util.ArrayList
import com.clevertap.ct_templates.nd.coachmark.CoachMarkHelper

class RestaurantActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityRestaurantBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private val TAG = "RestaurantActivity"

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)

//        cleverTapDefaultInstance?.pushEvent("Native Display Event")

        callProdExp()
    }

    private fun callProdExp() {
        cleverTapDefaultInstance!!.syncVariables()

        cleverTapDefaultInstance!!.fetchVariables {
            val valueFetched = cleverTapDefaultInstance!!.getVariableValue("pe_coachmarks").toString()
            Log.d(TAG, "ThemeValues: $valueFetched")
            try {
                val jsonObject = JSONObject(valueFetched)
                runOnUiThread {
                    CoachMarkHelper().renderCoachMark(this, jsonObject) {
                        println("Coachmark completed successfully")
                        Log.d(TAG, "Coachmark completed successfully via PE")
                    }
                }
            } catch (e: Exception) {
                Log.e("JSON_ERROR", "Error parsing JSON", e)
            }

        }
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
            println("unit.jsonObject : ${unit.jsonObject}")
            CoachMarkHelper().renderCoachMark(this, unit.jsonObject){
                CleverTapAPI.getDefaultInstance(this@RestaurantActivity)?.pushDisplayUnitClickedEventForID(unit.unitID)
            }
        } else {
            println("NA")
        }
    }
}

