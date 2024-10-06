package com.project.integrationsdk

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.databinding.ActivityNativeDisplayBinding
//import com.synnapps.carouselview.CarouselView
//import com.synnapps.carouselview.ImageListener
//import com.synnapps.carouselview.ViewListener
import java.util.ArrayList

class NativeDisplayActivity : AppCompatActivity(), DisplayUnitListener {

    private lateinit var binding: ActivityNativeDisplayBinding
//    private lateinit var carouselView: CarouselView
//    private lateinit var customCarouselView: CarouselView
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private lateinit var sampleImage: ArrayList<String>
    private lateinit var sampleTitle: ArrayList<String>
    private lateinit var sampleLink: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNativeDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sampleImage = ArrayList()
        sampleTitle = ArrayList()
        sampleLink = ArrayList()

//        carouselView = binding.carouselView
//        customCarouselView = binding.customCarouselView
//
//        carouselView.pageCount = sampleImage.size
//        customCarouselView.pageCount = sampleImage.size
//        customCarouselView.slideInterval = 5000
//
//        carouselView.setImageListener(imageListener)
//        customCarouselView.setViewListener(viewListener)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)
    }

//    private var imageListener: ImageListener =
//        ImageListener { position, imageView ->
//            Glide.with(this).load(sampleImage[position]).into(imageView)
//        }
//
//    private var viewListener = ViewListener { position ->
//        val customView = layoutInflater.inflate(R.layout.view_custom, null)
//        val ndTitle = customView.findViewById<TextView>(R.id.ndTextView)
//        val ndImageView = customView.findViewById<ImageView>(R.id.ndImageView)
//        Glide.with(applicationContext).load(sampleImage[position]).into(ndImageView)
//        ndTitle.text = sampleTitle[position]
//        customView
//    }

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
//            binding.nativeDisplayTitle.text = it.title.toString()
//            binding.nativeDisplayMessage.text = it.message.toString()
//            binding.nativeDisplayTitle.setTextColor(Color.parseColor(it.titleColor))
//            binding.nativeDisplayMessage.setTextColor(Color.parseColor(it.messageColor))
            println(it.media)
            sampleImage.add(it.media)
            sampleTitle.add(it.title)
            sampleLink.add(it.actionUrl)
        }

//        carouselView.pageCount = sampleImage.size
//        carouselView.setImageListener(imageListener)
//
//        customCarouselView.pageCount = sampleImage.size
//        customCarouselView.setViewListener(viewListener)
//
//        carouselView.setImageClickListener {
//            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
//                Toast.makeText(applicationContext, "Event Card Clicked!", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        customCarouselView.setImageClickListener {
//            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
//                Toast.makeText(applicationContext, "Event Card custom Clicked!", Toast.LENGTH_SHORT).show()
//            }
//            for(i in 1..5){
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sampleLink[i])))
//            }
//        }

        //Notification Viewed Event
        CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)

        //Notification Clicked Event
//        binding.nativeDisplayCardView.setOnClickListener {
//            CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
//                Toast.makeText(applicationContext, "Event Card Clicked!", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}