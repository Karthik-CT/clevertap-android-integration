package com.project.integrationsdk.utils

import android.content.Context
import java.util.UUID

object CleverTapIdManager {

    private const val PREF_NAME        = "ct_prefs"
    private const val KEY_CT_ID        = "clevertap_id"
    private const val KEY_HAS_IDENTITY = "has_real_identity" // false = still anonymous

    fun getOrCreateId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var ctId = prefs.getString(KEY_CT_ID, null)
        if (ctId == null) {
            ctId = generateId()
            prefs.edit().putString(KEY_CT_ID, ctId).apply()
        }
        return ctId
    }

    fun saveId(context: Context, id: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CT_ID, id)
            .putBoolean(KEY_HAS_IDENTITY, true)
            .apply()
    }

    fun isFirstTimeSignup(context: Context): Boolean {
        return !context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAS_IDENTITY, false)
    }

    fun generateId(): String {
        val random = UUID.randomUUID().toString().replace("-", "").take(20)
        return "$random-sp"
    }
}