package com.project.integrationsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.inbox.CTInboxMessage
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
        customAIAdapter = CustomAIAdapter(allMessage, applicationContext)
        binding.customAppInboxRv.adapter = customAIAdapter

        cleverTapDefaultInstance?.apply {
            allMessage.forEach {
                pushInboxNotificationViewedEvent(it.messageId)
                pushInboxNotificationClickedEvent(it.messageId)
            }
        }

        binding.customAppInboxGetMessagesbtn.setOnClickListener{
            val allMessage = cleverTapDefaultInstance!!.allInboxMessages
            customAIAdapter = CustomAIAdapter(allMessage, applicationContext)
            binding.customAppInboxRv.adapter = customAIAdapter
        }

        binding.customAppInboxRaiseEvent.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's App Inbox Event")
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