package com.project.integrationsdk.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.project.integrationsdk.adapter.KFCNDAdapter
import com.project.integrationsdk.databinding.ActivityKfcnativeDisplayBinding
import com.project.integrationsdk.model.KFCNDModel
import org.json.JSONObject

class KFCNativeDisplayActivity : AppCompatActivity(), DisplayUnitListener {

    private lateinit var binding: ActivityKfcnativeDisplayBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private val recItems = mutableListOf<KFCNDModel>()
    private val offerItems = mutableListOf<KFCNDModel>()
    private var recAdapter: KFCNDAdapter? = null
    private var offerAdapter: KFCNDAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKfcnativeDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.setDisplayUnitListener(this)

        cleverTapDefaultInstance?.pushEvent("KFCNativeDisplay")
        cleverTapDefaultInstance?.pushEvent("KFCOfferNativeDisplay")

        binding.rvProducts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvOffer .layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        units?.forEach { unit ->
            Log.d("KFCND", "UNIT LOADED: ${unit.unitID}")
            prepareDisplayView(unit)
        }
    }

    private fun prepareDisplayView(unit: CleverTapDisplayUnit) {
        val extras = unit.customExtras ?: return
        val keys = extras.keys
        val isOfferUnit = keys.any { it.startsWith("offerRecommendation", ignoreCase = true) }
        val isRecUnit = keys.any { it.startsWith("recommendation", ignoreCase = true) }
        val parsedItems = parseKFCRecommendations(unit)
        runOnUiThread {
            if (isOfferUnit && parsedItems.isNotEmpty()) {
                val startIndex = offerItems.size
                offerItems.addAll(parsedItems)
                if (offerAdapter == null) {
                    offerAdapter = KFCNDAdapter(
                        offerItems,
                        onAddToCart = { selected ->
                            Toast.makeText(this, "${selected.title} added!", Toast.LENGTH_SHORT)
                                .show()
                        },
                        context = this
                    )
                    binding.rvOffer.adapter = offerAdapter
                } else {
                    offerAdapter?.notifyItemRangeInserted(startIndex, parsedItems.size)
                }

            } else if (isRecUnit && parsedItems.isNotEmpty()) {
                val startIndex = recItems.size
                recItems.addAll(parsedItems)
                if (recAdapter == null) {
                    recAdapter = KFCNDAdapter(
                        recItems,
                        onAddToCart = { selected ->
                            Toast.makeText(this, "${selected.title} added!", Toast.LENGTH_SHORT)
                                .show()
                        },
                        context = this
                    )
                    binding.rvProducts.adapter = recAdapter
                } else {
                    recAdapter?.notifyItemRangeInserted(startIndex, parsedItems.size)
                }
            } else {
                if (parsedItems.isNotEmpty()) {
                    val startIndex = recItems.size
                    recItems.addAll(parsedItems)
                    if (recAdapter == null) {
                        recAdapter = KFCNDAdapter(
                            recItems,
                            onAddToCart = { selected ->
                                Toast.makeText(this, "${selected.title} added!", Toast.LENGTH_SHORT)
                                    .show()
                            },
                            context = this
                        )
                        binding.rvProducts.adapter = recAdapter
                    } else {
                        recAdapter?.notifyItemRangeInserted(startIndex, parsedItems.size)
                    }
                }
            }
        }
    }

    private fun parseKFCRecommendations(unit: CleverTapDisplayUnit): List<KFCNDModel> {
        val result = mutableListOf<KFCNDModel>()
        val extras = unit.customExtras ?: return result
        val candidateKeys = extras.keys.filter { key ->
            key.startsWith("recommendation", ignoreCase = true) ||
                    key.startsWith("offerRecommendation", ignoreCase = true)
        }.sortedBy { key ->
            val numPart = key.replace(Regex("[^0-9]"), "")
            numPart.toIntOrNull() ?: Int.MAX_VALUE
        }

        candidateKeys.forEach { key ->
            val value = extras[key]
            if (value is String) {
                try {
                    val json = JSONObject(value)
                    val imageUrl = json.optString("imageURL", json.optString("ImageURL", json.optString("image", "")))
                    val title = json.optString("title", "")
                    val desc = json.optString("desc", "")
                    val price = json.optString("price", "")
                    result.add(
                        KFCNDModel(
                            imageRes = imageUrl,
                            title = title,
                            description = desc,
                            price = price
                        )
                    )
                } catch (e: Exception) {
                    Log.e("KFCND", "Failed to parse key: $key value: $value", e)
                }
            }
        }
        return result
    }
}
