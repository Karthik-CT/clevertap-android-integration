package com.project.integrationsdk.utils

import android.content.Context
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapManager {

    private const val TAG = "CleverTapManager"
    private var cleverTapAPI: CleverTapAPI? = null
    var ctId: String? = null

    fun init(context: Context) {
        ctId = CleverTapIdManager.getOrCreateId(context)
        cleverTapAPI = CleverTapAPI.getDefaultInstance(context, ctId)
        Log.d(TAG, "CT init → ctId: $ctId")
    }

    fun getInstance(): CleverTapAPI? = cleverTapAPI

    fun onUserLogin(profile: Map<String, Any>, clevertapID: String, context: Context) {
        ctId = clevertapID
        CleverTapIdManager.saveId(context, clevertapID)
        Log.d(TAG, "onUserLogin (with CT ID) → $clevertapID → dashboard: __h$clevertapID")
        cleverTapAPI?.onUserLogin(profile, clevertapID)
    }

    fun onFirstSignup(profile: Map<String, Any>, clevertapID: String, context: Context) {
        ctId = clevertapID
        CleverTapIdManager.saveId(context, clevertapID)
        cleverTapAPI?.onUserLogin(profile)
    }

    fun pushEvent(eventName: String, props: Map<String, Any>? = null) {
        val finalProps = HashMap<String, Any>()
        props?.let { finalProps.putAll(it) }
        ctId?.let { finalProps["userID"] = it }
        cleverTapAPI?.pushEvent(eventName, finalProps)
    }
}