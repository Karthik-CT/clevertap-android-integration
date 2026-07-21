package com.project.integrationsdk.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.databinding.ActivityUploadEventsBinding
import com.project.integrationsdk.util.bindInboxUnreadCount
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject
import java.util.Date

class UploadEventsActivity : AppCompatActivity(), CTInboxListener {

    lateinit var binding: ActivityUploadEventsBinding
    private val ctInstance by lazy {
        CleverTapManager.getInstance(this)
    }
    private val ct by lazy { CleverTapManager.getInstance(applicationContext) }

    private var propertyCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadEventsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        val insetsController = WindowCompat.getInsetsController(window, binding.root)
        insetsController.isAppearanceLightStatusBars = false  // false = white icons on dark bg

        applyStatusBarInset()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnAdd.setOnClickListener {
            addKeyValueRow()
        }

        binding.btnAddProperty.setOnClickListener {
            addKeyValueRow()
        }

        binding.btnSubmit.setOnClickListener {
            raiseCustomEvent()
        }

        binding.btnNotification.setOnClickListener {
            toast("App Inbox")
            ct?.showAppInbox()
        }

        binding.multiValueArrayEventButton.setOnClickListener {
//            val eventProperties = HashMap<String, Any>()
//            eventProperties["Product ID"] = arrayListOf("7896", "7586")
//            ct?.pushEvent("addToCart", eventProperties)
//            Toast.makeText(applicationContext, "PUSHED MULTI VALUE ARRAY EVENT", Toast.LENGTH_SHORT).show()

//            val charges = hashMapOf<String, Any>("Total Amount" to 400)
//            val items = arrayListOf(
//                hashMapOf<String, Any>(
//                    "Item name" to "Burger",
//                    "Number of Items" to 1,
//                    "Amount" to 200
//                ),
//                hashMapOf<String, Any>(
//                    "Item name" to "Pepsi",
//                    "Number of Items" to 1,
//                    "Amount" to 100
//                )
//            )
//            ct?.pushChargedEvent(charges, items)

            val bundlePurchased = hashMapOf<String, Any>(
                "bundle_id" to "INT_5GB_7D",
                "bundle_name" to "5GB Weekly Internet",
                "bundle_category" to "Internet",
                "bundle_price" to 3000,
                "currency" to "IQD",
                "validity_days" to 7,
                "purchase_date" to Date(),
                "expiry_date" to Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)),
                "transaction_id" to "TXN987654321",
                "renewable" to true,
                "auto_renew" to false,
                "offer_type" to "Regular",
                "language" to "Arabic",
                "purchase_channel" to "App"
            )
            ct?.pushEvent("purchase_bundle_completed", bundlePurchased)
            Toast.makeText(applicationContext, "purchase_bundle_completed Clicked", Toast.LENGTH_SHORT).show()
        }

        setupInboxBadge()

        updatePropertyCountBadge()
    }

    private fun setupInboxBadge() {
        ct?.apply {
            ctNotificationInboxListener = this@UploadEventsActivity
            initializeInbox()
        }
        updateInboxBadge()
    }

    private fun updateInboxBadge() {
        binding.notifBadge.bindInboxUnreadCount(ct)
    }

    override fun inboxDidInitialize() {
        runOnUiThread { updateInboxBadge() }
    }

    override fun inboxMessagesDidUpdate() {
        runOnUiThread { updateInboxBadge() }
    }

    override fun onResume() {
        super.onResume()
        updateInboxBadge()
    }

    private fun toast(msg: String) =
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()


    private fun applyStatusBarInset() {
        val extraPadding = (14 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(
                view.paddingLeft,
                statusBarHeight + extraPadding,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.toolbarContainer)
    }

    private fun addKeyValueRow() {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_key_value, binding.keyValueContainer, false)
        row.findViewById<View>(R.id.btnDeleteRow).setOnClickListener {
            binding.keyValueContainer.removeView(row)
            propertyCount--
            updatePropertyCountBadge()
        }
        binding.keyValueContainer.addView(row)
        propertyCount++
        updatePropertyCountBadge()
    }

    private fun updatePropertyCountBadge() {
        binding.tvPropertyCount.text = "$propertyCount added"
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
        ctInstance?.pushEvent(eventName, eventProps)
        Toast.makeText(applicationContext, "${eventName} raised", Toast.LENGTH_SHORT).show()
    }

    private fun parseValue(value: String): Any {

        return when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false

            value.toIntOrNull() != null -> value.toInt()
            value.toLongOrNull() != null -> value.toLong()
            value.toFloatOrNull() != null -> value.toFloat()
            value.toDoubleOrNull() != null -> value.toDouble()

            else -> value
        }
    }
}
