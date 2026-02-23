package com.project.integrationsdk.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivityMultiAppBinding
import kotlin.toString

class MultiAppActivity : AppCompatActivity() {

    lateinit var binding: ActivityMultiAppBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdd.setOnClickListener {
            addKeyValueRow()
        }

        binding.btnSubmit.setOnClickListener {
            raiseCustomEvent()
        }

        binding.productViewedButton.setOnClickListener {
            raiseProductViewed()
        }

        binding.addToCartButton.setOnClickListener {
            raiseAddToCart()
        }

        binding.purchaseButton.setOnClickListener {
            raisePurchaseEvent()
        }

    }

    private fun addKeyValueRow() {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_key_value, binding.keyValueContainer, false)
        binding.keyValueContainer.addView(row)
    }

    private fun raiseProductViewed() {
        val prodViewedAction1 = mapOf(
            "productPriceCurrency" to "AED",
            "productPrice" to  34,
            "productCategoryId" to 8598,
            "productName" to "Twister Box",
            "productCategoryName" to "Twisters",
            "productId" to "KFCTW008"
        )
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent("Product Viewed", prodViewedAction1)
        Toast.makeText(applicationContext, "${binding.productViewedButton.text} clicked", Toast.LENGTH_SHORT).show()
    }


    private fun raiseAddToCart() {
        val prodViewedAction1 = mapOf(
            "productPriceCurrency" to "AED",
            "productPrice" to  34,
            "productCategoryId" to 8598,
            "productName" to "Twister Box",
            "productCategoryName" to "Twisters",
            "productId" to "KFCTW008"
        )
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent("Added to Cart", prodViewedAction1)
        Toast.makeText(applicationContext, "${binding.addToCartButton.text} clicked", Toast.LENGTH_SHORT).show()
    }

    private fun raisePurchaseEvent() {
        val prodViewedAction1 = mapOf(
            "productPriceCurrency" to "AED",
            "productPrice" to  34,
            "productCategoryId" to 8598,
            "productName" to "Twister Box",
            "productCategoryName" to "Twisters",
            "productId" to "KFCTW008",
            "grandTotal" to 50
        )
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent("Purchase", prodViewedAction1)
        Toast.makeText(applicationContext, "${binding.purchaseButton.text} clicked", Toast.LENGTH_SHORT).show()
    }

    private fun raiseCustomEvent() {
        val eventName = binding.etEventName.text.toString().trim()
        if (eventName.isEmpty()) return

        val eventProps = HashMap<String, Any>()

        for (i in 0 until binding.keyValueContainer.childCount) {
            val row = binding.keyValueContainer.getChildAt(i)
            val etKey = row.findViewById<EditText>(R.id.etKey)
            val etValue = row.findViewById<EditText>(R.id.etValue)
            val key = etKey.text.toString().trim()
            val value = etValue.text.toString().trim()

            if (key.isNotEmpty() && value.isNotEmpty()) {
                eventProps[key] = parseValue(value)
            }
        }

        Log.d("MultiActivity", "raiseCustomEvent: ${eventProps}")
//        CleverTapAPI.getDefaultInstance(applicationContext)?.pushEvent(eventName, eventProps)
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushProfile(eventProps)
        Toast.makeText(applicationContext, "${eventName} raised", Toast.LENGTH_SHORT).show()
    }

    private fun parseValue(value: String): Any {
        return value.toIntOrNull()
            ?: value.toDoubleOrNull()
            ?: value
    }

}