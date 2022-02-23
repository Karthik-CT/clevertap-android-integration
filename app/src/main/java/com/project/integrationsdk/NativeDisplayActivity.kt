package com.project.integrationsdk

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.databinding.ActivityNativeDisplayBinding
import java.util.ArrayList

class NativeDisplayActivity : AppCompatActivity(), DisplayUnitListener {

    lateinit var binding: ActivityNativeDisplayBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)
    }

    override fun onDisplayUnitsLoaded(units: java.util.ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            println(unit)
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        println("prepareDisplayView: $unit")
        println("title: ${unit.contents[0].title} and Message: ${unit.contents[0].message}")
        unit.contents.forEach {
            binding.nativeDisplayTitle.text = it.title.toString()
            binding.nativeDisplayMessage.text = it.message.toString()
            binding.nativeDisplayTitle.setTextColor(Color.parseColor(it.titleColor))
            binding.nativeDisplayMessage.setTextColor(Color.parseColor(it.messageColor))
        }

        //Notification Viewed Event
        CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)

        //Notification Clicked Event
        binding.nativeDisplayCardView.setOnClickListener {
            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
                Toast.makeText(applicationContext, "Event Card Clicked!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}