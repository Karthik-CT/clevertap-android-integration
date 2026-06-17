package com.project.integrationsdk.ui

import android.content.Context
import android.os.Bundle
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
import com.project.integrationsdk.databinding.ActivityUploadUserPropertiesBinding
import com.project.integrationsdk.util.bindInboxUnreadCount

class UploadUserPropertiesActivity : AppCompatActivity(), CTInboxListener {

    lateinit var binding: ActivityUploadUserPropertiesBinding
    private val ctInstance by lazy {
        CleverTapManager.getInstance(this)
    }
    private val ct by lazy { CleverTapManager.getInstance(applicationContext) }

    private var propertyCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadUserPropertiesBinding.inflate(layoutInflater)
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
            raiseUserProperty()
        }

        binding.btnNotification.setOnClickListener {
            toast("App Inbox")
            ct?.showAppInbox()
        }

        setupInboxBadge()

        updatePropertyCountBadge()
    }

    // Initialize the App Inbox and render the unread count
    // (ct?.inboxMessageUnreadCount) on top of the notification bell.
    private fun setupInboxBadge() {
        ct?.apply {
            ctNotificationInboxListener = this@UploadUserPropertiesActivity
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
        val row = LayoutInflater.from(this).inflate(R.layout.item_key_value, binding.keyValueContainer, false)
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

    private fun raiseUserProperty() {
        val userProps = HashMap<String, Any>()
        val mainKey = binding.etMainKey.text.toString().trim()
        val mainValue = binding.etMainValue.text.toString().trim()
        if (mainKey.isNotEmpty() && mainValue.isNotEmpty()) {
            userProps[mainKey] = parseValue(mainValue)
        }
        for (i in 0 until binding.keyValueContainer.childCount) {
            val row = binding.keyValueContainer.getChildAt(i)
            val etKey = row.findViewById<EditText>(R.id.etKey)
            val etValue = row.findViewById<EditText>(R.id.etValue)
            val key = etKey.text.toString().trim()
            val value = etValue.text.toString().trim()
            if (key.isNotEmpty() && value.isNotEmpty()) {
                userProps[key] = parseValue(value)
            }
        }
        if (userProps.isEmpty()) {
            Toast.makeText(this, "Please enter at least one property", Toast.LENGTH_SHORT).show()
            return
        }
        ctInstance?.pushProfile(userProps)
        Toast.makeText(this, "User property uploaded", Toast.LENGTH_SHORT).show()
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
