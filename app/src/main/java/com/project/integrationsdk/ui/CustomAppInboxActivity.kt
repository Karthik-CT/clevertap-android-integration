package com.project.integrationsdk.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.adapter.CustomAIAdapter
import com.project.integrationsdk.databinding.ActivityCustomAppInboxBinding

class CustomAppInboxActivity : AppCompatActivity(), CTInboxListener {

    private lateinit var binding: ActivityCustomAppInboxBinding
    private val cleverTap: CleverTapAPI? by lazy {
        CleverTapAPI.getDefaultInstance(
            applicationContext
        )
    }
    private lateinit var adapter: CustomAIAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomAppInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)

        setupRecyclerView()
        markUnreadMessagesAsViewed()

        binding.customAppInboxGetMessagesbtn.setOnClickListener { refreshMessages() }
        binding.customAppInboxRaiseEvent.setOnClickListener { raiseCustomEvent() }
    }

    private fun setupRecyclerView() {
        adapter = CustomAIAdapter(cleverTap?.allInboxMessages ?: arrayListOf(), this) { msg ->
            // To raise App Inbox Notification Clicked event
            cleverTap?.pushInboxNotificationClickedEvent(msg.messageId)
            Toast.makeText(this, "Clicked: ${msg.messageId}", Toast.LENGTH_SHORT).show()
        }

        binding.customAppInboxRv.apply {
            layoutManager = LinearLayoutManager(this@CustomAppInboxActivity)
            setHasFixedSize(true)
            adapter = this@CustomAppInboxActivity.adapter
        }
    }

    private fun markUnreadMessagesAsViewed() {
        cleverTap?.unreadInboxMessages?.forEach {
            // To raise App Inbox Notification Viewed event
            cleverTap?.pushInboxNotificationViewedEvent(it.messageId)
            //To mark the message as read
            cleverTap?.markReadInboxMessage(it.messageId)
        }
    }

    private fun refreshMessages() {
        adapter = CustomAIAdapter(cleverTap?.allInboxMessages ?: arrayListOf(), this) { msg ->
            cleverTap?.pushInboxNotificationClickedEvent(msg.messageId)
            Toast.makeText(this, "Clicked: ${msg.messageId}", Toast.LENGTH_SHORT).show()
        }
        binding.customAppInboxRv.adapter = adapter
    }

    private fun raiseCustomEvent() {
        cleverTap?.pushEvent("App Inbox Event")
        Toast.makeText(this, "Custom App Inbox button Clicked", Toast.LENGTH_SHORT).show()
    }

    override fun inboxDidInitialize() {}
    override fun inboxMessagesDidUpdate() {}
}
