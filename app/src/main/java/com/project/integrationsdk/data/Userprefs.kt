package com.project.integrationsdk.data

import android.content.Context
import java.util.Date

/**
 * UserPrefs — single source of truth for all user session + profile data.
 *
 * Replaces SessionManager entirely. One SharedPrefs file, one object,
 * one place to look whenever you need anything about the current user.
 *
 * USAGE:
 *   Login:    UserPrefs.login(context, profile)
 *   Logout:   UserPrefs.logout(context)
 *   Check:    UserPrefs.isLoggedIn(context)
 *   Read:     UserPrefs.load(context).firstName
 *   Update:   UserPrefs.save(context, updatedProfile)
 */
object UserPrefs {

    // Per-dashboard prefs file: "user_profile_<dashboardId>". Each dashboard
    // keeps its own login flag + profile so switching accounts in Settings
    // shows Login only when the user has never signed in on that account,
    // and goes straight home when returning to a dashboard they were
    // already signed into.
    private const val PREFS_PREFIX = "user_profile_"

    // ── Keys ──────────────────────────────────────────────────────
    private const val KEY_IS_LOGGED_IN  = "is_logged_in"   // ← was only in SessionManager
    private const val KEY_FIRST_NAME    = "firstName"
    private const val KEY_LAST_NAME     = "lastName"
    private const val KEY_EMAIL         = "email"
    private const val KEY_PHONE         = "phone"
    private const val KEY_IDENTITY      = "identity"
    private const val KEY_GENDER        = "gender"
    private const val KEY_DOB_MILLIS    = "dobMillis"
    private const val KEY_MSG_EMAIL     = "msgEmail"
    private const val KEY_MSG_PUSH      = "msgPush"
    private const val KEY_MSG_SMS       = "msgSms"
    private const val KEY_MSG_WA        = "msgWhatsapp"

    // ─────────────────────────────────────────────────────────────
    //  DATA CLASS
    // ─────────────────────────────────────────────────────────────

    data class Profile(
        val firstName:   String  = "",
        val lastName:    String  = "",
        val email:       String  = "",
        val phone:       String  = "",
        val identity:    String  = "",
        val gender:      String  = "",  // "M", "F", or "O"
        val dob:         Date?   = null,
        val msgEmail:    Boolean = true,
        val msgPush:     Boolean = true,
        val msgSms:      Boolean = false,
        val msgWhatsapp: Boolean = false
    ) {
        val fullName: String get() = "$firstName $lastName".trim()

        val initials: String get() = buildString {
            if (firstName.isNotEmpty()) append(firstName.first().uppercaseChar())
            if (lastName.isNotEmpty())  append(lastName.first().uppercaseChar())
            if (isEmpty()) append("?")
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  LOGIN
    //  Call this once after successful authentication.
    //  Sets is_logged_in = true AND saves profile data together.
    // ─────────────────────────────────────────────────────────────

    fun login(context: Context, profile: Profile) {
        prefs(context).edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)   // ← the flag SessionManager was setting
            .putString(KEY_FIRST_NAME,  profile.firstName)
            .putString(KEY_LAST_NAME,   profile.lastName)
            .putString(KEY_EMAIL,       profile.email)
            .putString(KEY_PHONE,       profile.phone)
            .putString(KEY_IDENTITY,    profile.identity)
            .putString(KEY_GENDER,      profile.gender)
            .putLong(KEY_DOB_MILLIS,    profile.dob?.time ?: -1L)
            .putBoolean(KEY_MSG_EMAIL,  profile.msgEmail)
            .putBoolean(KEY_MSG_PUSH,   profile.msgPush)
            .putBoolean(KEY_MSG_SMS,    profile.msgSms)
            .putBoolean(KEY_MSG_WA,     profile.msgWhatsapp)
            .apply()
    }

    // ─────────────────────────────────────────────────────────────
    //  SAVE
    //  Call this to update profile fields WITHOUT changing the
    //  is_logged_in flag (e.g. from ProfileFragment after editing).
    // ─────────────────────────────────────────────────────────────

    fun save(context: Context, profile: Profile) {
        prefs(context).edit()
            // Note: is_logged_in is NOT touched here — preserves session state
            .putString(KEY_FIRST_NAME,  profile.firstName)
            .putString(KEY_LAST_NAME,   profile.lastName)
            .putString(KEY_EMAIL,       profile.email)
            .putString(KEY_PHONE,       profile.phone)
            .putString(KEY_IDENTITY,    profile.identity)
            .putString(KEY_GENDER,      profile.gender)
            .putLong(KEY_DOB_MILLIS,    profile.dob?.time ?: -1L)
            .putBoolean(KEY_MSG_EMAIL,  profile.msgEmail)
            .putBoolean(KEY_MSG_PUSH,   profile.msgPush)
            .putBoolean(KEY_MSG_SMS,    profile.msgSms)
            .putBoolean(KEY_MSG_WA,     profile.msgWhatsapp)
            .apply()
    }

    // ─────────────────────────────────────────────────────────────
    //  LOAD
    // ─────────────────────────────────────────────────────────────

    fun load(context: Context): Profile {
        val p = prefs(context)
        val dobMillis = p.getLong(KEY_DOB_MILLIS, -1L)
        return Profile(
            firstName    = p.getString(KEY_FIRST_NAME, "") ?: "",
            lastName     = p.getString(KEY_LAST_NAME,  "") ?: "",
            email        = p.getString(KEY_EMAIL,      "") ?: "",
            phone        = p.getString(KEY_PHONE,      "") ?: "",
            identity     = p.getString(KEY_IDENTITY,   "") ?: "",
            gender       = p.getString(KEY_GENDER,     "") ?: "",
            dob          = if (dobMillis != -1L) Date(dobMillis) else null,
            msgEmail     = p.getBoolean(KEY_MSG_EMAIL, true),
            msgPush      = p.getBoolean(KEY_MSG_PUSH,  true),
            msgSms       = p.getBoolean(KEY_MSG_SMS,   false),
            msgWhatsapp  = p.getBoolean(KEY_MSG_WA,    false)
        )
    }

    // ─────────────────────────────────────────────────────────────
    //  LOGOUT — clears everything including the is_logged_in flag
    // ─────────────────────────────────────────────────────────────

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }

    // ─────────────────────────────────────────────────────────────
    //  IS LOGGED IN — checks the explicit flag, not just email presence
    // ─────────────────────────────────────────────────────────────

    fun isLoggedIn(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_LOGGED_IN, false)

    // ─────────────────────────────────────────────────────────────
    //  PRIVATE HELPER
    // ─────────────────────────────────────────────────────────────

    private fun prefs(context: Context) =
        context.getSharedPreferences(prefsName(context), Context.MODE_PRIVATE)

    private fun prefsName(context: Context): String =
        PREFS_PREFIX + DashboardConfig.getActive(context).id
}