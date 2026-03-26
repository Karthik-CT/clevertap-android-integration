package com.project.integrationsdk.utils

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapManager {

    private var cleverTapAPI: CleverTapAPI? = null
    private var ctId: String? = null

    fun init(context: Context) {
        ctId = CleverTapIdManager.getOrCreateId(context)
        cleverTapAPI = CleverTapAPI.getDefaultInstance(context, ctId)
    }

    fun getInstance(): CleverTapAPI? {
        return cleverTapAPI
    }

    fun pushEvent(eventName: String, props: Map<String, Any>? = null) {
        val finalProps = HashMap<String, Any>()
        props?.let { finalProps.putAll(it) }
        ctId?.let { finalProps["userID"] = it }
        cleverTapAPI?.pushEvent(eventName, finalProps)
    }
}