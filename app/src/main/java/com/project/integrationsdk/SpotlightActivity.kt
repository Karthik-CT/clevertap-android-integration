package com.project.integrationsdk

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.spotlight.OnSpotlightListener
import com.project.integrationsdk.spotlight.OnTargetListener
import com.project.integrationsdk.spotlight.Spotlight
import com.project.integrationsdk.spotlight.SpotlightHelper
import com.project.integrationsdk.spotlight.Target
import com.project.integrationsdk.spotlight.effect.RippleEffect
import com.project.integrationsdk.spotlight.shape.Circle

class SpotlightActivity : AppCompatActivity(), DisplayUnitListener {

    private var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotlight)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        cleverTapDefaultInstance?.pushEvent("Spotlight Event")

        cleverTapDefaultInstance?.setDisplayUnitListener(this)
    }

    override fun onDisplayUnitsLoaded(units: java.util.ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        println("Spotlight unit: $unit")
        unit.customExtras.forEach { (key, value) ->
            println("$key: $value")
        }

        if (unit.customExtras["nd_id"] == "nd_spotlight") {
            SpotlightHelper().showSpotlight(this@SpotlightActivity, unit.jsonObject) {
                Log.d("Spotlight", "Clicked")
            }
        } else {
            println("NA")
        }
    }
}