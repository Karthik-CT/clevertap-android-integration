package com.project.integrationsdk.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.adapter.CustomAIAdapter
import com.project.integrationsdk.databinding.ActivityCustomAppInboxBinding

class CustomAppInboxActivity : AppCompatActivity(), CTInboxListener {
    lateinit var binding: ActivityCustomAppInboxBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null
    lateinit var customAIAdapter: CustomAIAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomAppInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        val allMessage = cleverTapDefaultInstance!!.allInboxMessages

        binding.customAppInboxRv.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        binding.customAppInboxRv.layoutManager = linearLayoutManager
        customAIAdapter = CustomAIAdapter(allMessage, applicationContext) { msg ->
            // To raise App Inbox Notification Clicked event
            cleverTapDefaultInstance?.pushInboxNotificationClickedEvent(msg.messageId)
            Toast.makeText(this, "Clicked: ${msg.messageId}", Toast.LENGTH_SHORT).show()
        }
        binding.customAppInboxRv.adapter = customAIAdapter

        cleverTapDefaultInstance?.unreadInboxMessages?.forEach {
            // To raise App Inbox Notification Viewed event
            cleverTapDefaultInstance?.pushInboxNotificationViewedEvent(it.messageId)
            //To mark the message as read
            cleverTapDefaultInstance?.markReadInboxMessage(it.messageId)
        }

        binding.customAppInboxGetMessagesbtn.setOnClickListener {
            val allMessage = cleverTapDefaultInstance!!.allInboxMessages
            binding.customAppInboxRv.adapter = customAIAdapter
        }

        binding.customAppInboxRaiseEvent.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("App Inbox Event")
            Toast.makeText(
                applicationContext,
                "Custom App Inbox button Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun inboxDidInitialize() {
    }

    override fun inboxMessagesDidUpdate() {
    }
}