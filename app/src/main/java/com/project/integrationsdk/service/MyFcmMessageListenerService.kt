package com.project.integrationsdk.service

import com.clevertap.android.sdk.pushnotification.fcm.CTFcmMessageHandler
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService


class MyFcmMessageListenerService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        CTFcmMessageHandler()
            .createNotification(applicationContext, message)
    }
}