package com.project.integrationsdk.ui

//import com.synnapps.carouselview.CarouselView
//import com.synnapps.carouselview.ImageListener
//import com.synnapps.carouselview.ViewListener
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.databinding.ActivityNativeDisplayBinding

class NativeDisplayActivity : AppCompatActivity(), DisplayUnitListener {

    private lateinit var binding: ActivityNativeDisplayBinding
//    private lateinit var carouselView: CarouselView
//    private lateinit var customCarouselView: CarouselView
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private lateinit var sampleImage: ArrayList<String>
    private lateinit var sampleTitle: ArrayList<String>
    private lateinit var sampleLink: ArrayList<String>

    private val TAG = "NativeDisplayActivity"

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

        cleverTapDefaultInstance = CleverTapManager.getInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)
        getAllDisplayUnits()
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

    private fun getAllDisplayUnits() {
        logStep("DISPLAY UNITS", "Getting all display units")
        printVar("All Display Units", cleverTapDefaultInstance?.allDisplayUnits.toString())
    }

    private fun logStep(section: String, description: String) {
        Log.i(TAG, "═══════════════════════════════════════════════════════")
        Log.i(TAG, "SECTION: $section")
        Log.i(TAG, "STEP: $description")
        Log.i(TAG, "═══════════════════════════════════════════════════════")
    }
    private fun printVar(name: String, value: Any?) {
        Log.i(TAG, "  ▶ $name: $value")
    }

    override fun onDisplayUnitsLoaded(units: java.util.ArrayList<CleverTapDisplayUnit>?) {
        for (i in 0 until units!!.size) {
            val unit = units[i]
            println("THis is ND: $unit")
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

            Glide.with(applicationContext).load(it.media).into(binding.imageNd)

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
//            CleverTapManager.getInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
//                Toast.makeText(applicationContext, "Event Card Clicked!", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        customCarouselView.setImageClickListener {
//            CleverTapManager.getInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
//                Toast.makeText(applicationContext, "Event Card custom Clicked!", Toast.LENGTH_SHORT).show()
//            }
//            for(i in 1..5){
//                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sampleLink[i])))
//            }
//        }

        //Notification Viewed Event
        CleverTapManager.getInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)

        //Notification Clicked Event
//        binding.nativeDisplayCardView.setOnClickListener {
//            CleverTapManager.getInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID).apply {
//                Toast.makeText(applicationContext, "Event Card Clicked!", Toast.LENGTH_SHORT).show()
//            }
//        }
    }
}