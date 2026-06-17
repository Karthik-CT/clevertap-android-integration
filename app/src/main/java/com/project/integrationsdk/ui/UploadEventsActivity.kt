package com.project.integrationsdk.ui

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
import com.project.integrationsdk.databinding.ActivityUploadEventsBinding
import com.project.integrationsdk.util.bindInboxUnreadCount

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

        setupInboxBadge()

        updatePropertyCountBadge()
    }

    // Initialize the App Inbox and render the unread count
    // (ct?.inboxMessageUnreadCount) on top of the notification bell.
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
