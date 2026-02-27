package com.project.integrationsdk.session

import android.content.Context

object SessionManager {

    private const val PREF_NAME = "ct_demo_pref"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_IDENTITY = "identity"
    private const val KEY_EMAIL = "email"
    private const val KEY_PHONE = "phone"
    private const val KEY_NAME= "phone"

    fun login(context: Context, identity: String, email: String, phone: String, name: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_IDENTITY, identity)
            .putString(KEY_EMAIL, email)
            .putString(KEY_NAME, name)
            .apply()
    }

    fun logout(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getIdentity(context: Context): Map<String, String?> {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "identity" to pref.getString(KEY_IDENTITY, null),
            "email" to pref.getString(KEY_EMAIL, null),
            "name" to pref.getString(KEY_NAME, null),
            "phone" to pref.getString(KEY_PHONE, null)
        )
    }
}