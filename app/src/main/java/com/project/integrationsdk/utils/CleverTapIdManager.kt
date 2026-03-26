package com.project.integrationsdk.utils

import android.content.Context
import java.util.UUID

object CleverTapIdManager {

    private const val PREF_NAME = "ct_prefs"
    private const val KEY_CT_ID = "clevertap_id"

    fun getOrCreateId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var ctId = prefs.getString(KEY_CT_ID, null)

        if (ctId == null) {
            ctId = generateId()
            prefs.edit().putString(KEY_CT_ID, ctId).apply()
        }

        return ctId
    }

    fun regenerateId(context: Context): String {
        val newId = generateId()
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CT_ID, newId).apply()
        return newId
    }

    fun generateId(): String {
        val random = UUID.randomUUID().toString().replace("-", "").take(20)
        return "$random-sp"
    }
}