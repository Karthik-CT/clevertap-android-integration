package com.project.integrationsdk

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.product_config.CTProductConfigListener
import com.project.integrationsdk.databinding.ActivityProductExperienceBinding

class ProductExperienceActivity : AppCompatActivity() {

    lateinit var binding: ActivityProductExperienceBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductExperienceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        productExperienceAB()
    }

    fun productExperienceAB() {
        val productConfigInstance = cleverTapDefaultInstance!!.productConfig()
        val hashMap = HashMap<String, Any>()
        hashMap["ProdExpKey"] = "product exp key test 100% original" //add these key value pair in dashboard keys
        productConfigInstance.setDefaults(hashMap)

        //FetchAndActivate
        productConfigInstance.fetchAndActivate()
        Log.d("pe", productConfigInstance.fetchAndActivate().toString())

        //register listener
        cleverTapDefaultInstance!!.setCTProductConfigListener(object : CTProductConfigListener {
            override fun onActivated() {
                val pe_act = cleverTapDefaultInstance!!.productConfig().getString("ProdExpKey")
                Log.d("pe_act", pe_act)
                when (pe_act) {
                    "product exp key test 100% original" -> {
                        binding.peTv.text = pe_act
                        binding.relLay.setBackgroundColor(Color.GREEN)
                    }
                    "this is variant A PE example" -> {
                        binding.peTv.text = pe_act
                        binding.peTv.setTextColor(Color.WHITE)
                        binding.relLay.setBackgroundColor(Color.BLUE)
                    }
                    "this is variant B PE example" -> {
                        binding.peTv.text = pe_act
                        binding.relLay.setBackgroundColor(Color.CYAN)
                    }
                }
            }

            override fun onFetched() {
                val pe_fet = cleverTapDefaultInstance!!.productConfig().getString("ProdExpKey")
                Log.d("pe_fet", pe_fet)
            }

            override fun onInit() {
                productConfigInstance.fetch()
                productConfigInstance.activate()
            }
        })

        //throttling
        productConfigInstance.setMinimumFetchIntervalInSeconds(60*10)

        //get parameter values
        productConfigInstance.getBoolean("key_bool")
        productConfigInstance.getDouble("key_double")
        productConfigInstance.getLong("key_long")
        productConfigInstance.getString("key_string")
        productConfigInstance.getString("key_json")

        //resetting the configs
//        productConfigInstance.reset()

        //last response time stamp
        productConfigInstance.lastFetchTimeStampInMillis
        Log.d("Product Experience", productConfigInstance.lastFetchTimeStampInMillis.toString())
    }

}