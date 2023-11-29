package com.project.integrationsdk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InboxMessageButtonListener
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.project.integrationsdk.adapter.CustomAIAdapter
import com.project.integrationsdk.databinding.ActivityCustomAppInboxBinding

class CustomAppInboxActivity : AppCompatActivity(), CTInboxListener, InboxMessageButtonListener {
    lateinit var binding: ActivityCustomAppInboxBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null
    lateinit var customAIAdapter: CustomAIAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomAppInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        cleverTapDefaultInstance?.apply {
            ctNotificationInboxListener = this@CustomAppInboxActivity
            initializeInbox()
        }
    }

    override fun inboxDidInitialize() {
        val allMessage = cleverTapDefaultInstance!!.allInboxMessages

        allMessage.forEach {
            println("Link Payload: ${it.inboxMessageContents[0].links}")
            //for getting custom KV pair
            val inbox = it.inboxMessageContents[0].links
            for (i in 0 until inbox.length()) {
                val inb = inbox.getJSONObject(i)
                println("KV value: ${inb.get("kv")}")
            }
        }

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
    }

    override fun inboxMessagesDidUpdate() {
    }

    override fun onInboxButtonClick(payload: HashMap<String, String>?) {
        payload?.apply {
            val key1 = get("key1")
            val key2 = get("key2")

            println("Keys: $key1 and $key2")
        }
        println("onInboxButtonClick called")
        println("Custom Inbox payload: $payload")
    }
}