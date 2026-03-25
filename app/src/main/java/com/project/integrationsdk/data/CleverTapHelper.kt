package com.project.integrationsdk.data

import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.data.UserPrefs.Profile

/**
 * CleverTapHelper — wraps every CleverTap SDK call in one place.
 *
 * WHY THIS EXISTS:
 * CleverTap has two distinct methods that are easy to confuse:
 *   1. onUserLogin()  — call ONCE when user logs in. Sets identity.
 *   2. pushProfile()  — call any time to update non-identity fields.
 *
 * Keeping these here means LoginActivity and ProfileFragment
 * never import or call the SDK directly — they just call these helpers.
 */
object CleverTapHelper {

    // ─────────────────────────────────────────────────────────────
    //  ON LOGIN
    //  Called from LoginActivity after the user authenticates.
    //  Sets the identity keys (Email + Identity) that CleverTap
    //  uses to recognize this person across devices/sessions.
    //  Also pushes the initial profile data collected at sign-up.
    //
    //  ⚠️  Do NOT call this on every app launch — only on login.
    //  ⚠️  Email and Identity are identity keys. Changing them
    //      after the first call can cause profile merges in CT.
    // ─────────────────────────────────────────────────────────────

    fun onLogin(ct: CleverTapAPI?, profile: Profile) {
        val map = HashMap<String, Any>()

        // Identity keys — tell CleverTap WHO this user is
        if (profile.email.isNotEmpty())    map["Email"]    = profile.email
        if (profile.identity.isNotEmpty()) map["Identity"] = profile.identity

        // Initial profile data collected at signup
        if (profile.fullName.isNotEmpty()) map["Name"]  = profile.fullName
        if (profile.phone.isNotEmpty())    map["Phone"] = profile.phone
        if (profile.gender == "M" || profile.gender == "F") map["Gender"] = profile.gender
        profile.dob?.let { map["DOB"] = it }

        // Default messaging preferences
        map["MSG-email"]    = profile.msgEmail
        map["MSG-push"]     = profile.msgPush
        map["MSG-sms"]      = profile.msgSms
        map["MSG-whatsapp"] = profile.msgWhatsapp

        ct?.onUserLogin(map)
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE PROFILE
    //  Called from ProfileFragment when user taps "Update Profile".
    //  Safe to call repeatedly — updates only the fields provided.
    //
    //  ⚠️  Email is intentionally NOT updated here.
    //      It is an identity key set at login and should not change.
    // ─────────────────────────────────────────────────────────────

    fun updateProfile(ct: CleverTapAPI?, profile: Profile) {
        val map = HashMap<String, Any>()

        if (profile.fullName.isNotEmpty()) map["Name"]  = profile.fullName
        if (profile.phone.isNotEmpty())    map["Phone"] = profile.phone

        // CleverTap only accepts "M" or "F" as reserved Gender values
        when (profile.gender) {
            "M", "F" -> map["Gender"] = profile.gender
            "O"      -> map["Gender_Custom"] = "Other"   // custom property
        }

        profile.dob?.let { map["DOB"] = it }

        // Messaging opt-in/out flags
        map["MSG-email"]    = profile.msgEmail
        map["MSG-push"]     = profile.msgPush
        map["MSG-sms"]      = profile.msgSms
        map["MSG-whatsapp"] = profile.msgWhatsapp

        ct?.pushProfile(map)
    }

    // ─────────────────────────────────────────────────────────────
    //  ON LOGOUT
    //  Resets the CleverTap session so the next user starts fresh.
    // ─────────────────────────────────────────────────────────────

    fun onLogout(ct: CleverTapAPI?) {
        // CleverTap doesn't have an explicit logout call —
        // calling onUserLogin with a new identity effectively
        // switches the user. For anonymous reset, use:
        ct?.pushEvent("User Logged Out")
        // Your app should then clear UserPrefs and navigate to Login.
    }
}
