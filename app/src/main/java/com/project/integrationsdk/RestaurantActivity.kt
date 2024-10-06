package com.project.integrationsdk

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.coachmark.CoachMarkSequence
import com.project.integrationsdk.coachmark.Gravity
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
            //Notification Viewed Event
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
            coachMarkSequence.apply {
                addItem(
                    targetView = binding.profileImage,
                    title = unit.customExtras["nd_title1"]!!,
                    subTitle = unit.customExtras["nd_subtitle1"]!!,
                    positiveButtonText = unit.customExtras["nd_positiveButtonText"]!!,
                    skipButtonText = unit.customExtras["nd_skipButtonText"]!!,
                    positiveButtonTextColor = Color.parseColor(unit.customExtras["nd_postiveBtnTextColor"]),
                    positiveButtonBGColor = Color.parseColor(unit.customExtras["nd_postiveBtnBackgroundColor"]),
                    skipButtonBGColor = Color.parseColor(unit.customExtras["nd_skipBtnBackgroundColor"]),
                    skipButtonTextColor = Color.parseColor(unit.customExtras["nd_skipBtnTextColor"])
                )
                addItem(
                    targetView = binding.search,
                    title = unit.customExtras["nd_title2"]!!,
                    subTitle = unit.customExtras["nd_subtitle2"]!!,
                    positiveButtonText = unit.customExtras["nd_positiveButtonText"]!!,
                    skipButtonText = unit.customExtras["nd_skipButtonText"]!!,
                    positiveButtonTextColor = Color.parseColor(unit.customExtras["nd_postiveBtnTextColor"]),
                    positiveButtonBGColor = Color.parseColor(unit.customExtras["nd_postiveBtnBackgroundColor"]),
                    skipButtonBGColor = Color.parseColor(unit.customExtras["nd_skipBtnBackgroundColor"]),
                    skipButtonTextColor = Color.parseColor(unit.customExtras["nd_skipBtnTextColor"])
                )
                addItem(
                    targetView = binding.cart,
                    title = unit.customExtras["nd_title3"]!!,
                    subTitle = unit.customExtras["nd_subtitle3"]!!,
                    positiveButtonText = unit.customExtras["nd_positiveButtonText"]!!,
                    skipButtonText = unit.customExtras["nd_skipButtonText"]!!,
                    positiveButtonTextColor = Color.parseColor(unit.customExtras["nd_postiveBtnTextColor"]),
                    positiveButtonBGColor = Color.parseColor(unit.customExtras["nd_postiveBtnBackgroundColor"]),
                    skipButtonBGColor = Color.parseColor(unit.customExtras["nd_skipBtnBackgroundColor"]),
                    skipButtonTextColor = Color.parseColor(unit.customExtras["nd_skipBtnTextColor"])
                )
                addItem(
                    targetView = binding.supportHelp,
                    title = unit.customExtras["nd_title4"]!!,
                    subTitle = unit.customExtras["nd_subtitle4"]!!,
                    positiveButtonText = unit.customExtras["nd_positiveButtonText"]!!,
                    skipButtonText = unit.customExtras["nd_skipButtonText"]!!,
                    positiveButtonTextColor = Color.parseColor(unit.customExtras["nd_postiveBtnTextColor"]),
                    positiveButtonBGColor = Color.parseColor(unit.customExtras["nd_postiveBtnBackgroundColor"]),
                    skipButtonBGColor = Color.parseColor(unit.customExtras["nd_skipBtnBackgroundColor"]),
                    skipButtonTextColor = Color.parseColor(unit.customExtras["nd_skipBtnTextColor"])
                )
                addItem(
                    targetView = binding.settings,
                    title = unit.customExtras["nd_title5"]!!,
                    subTitle = unit.customExtras["nd_subtitle5"]!!,
                    positiveButtonText = unit.customExtras["nd_finalPositiveButtonText"]!!,
                    skipButtonText = null,
                    positiveButtonTextColor = Color.parseColor(unit.customExtras["nd_postiveBtnTextColor"]),
                    positiveButtonBGColor = Color.parseColor(unit.customExtras["nd_postiveBtnBackgroundColor"])
                )
                start(window?.decorView as ViewGroup)
                setOnFinishCallback {
                    Toast.makeText(this@RestaurantActivity,getString(R.string.label_finish), Toast.LENGTH_SHORT).show()
                    //Notification Clicked Event
                    CleverTapAPI.getDefaultInstance(this@RestaurantActivity)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
                        Toast.makeText(applicationContext,"Event Card Clicked!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            println("NA")
        }


    }
}

