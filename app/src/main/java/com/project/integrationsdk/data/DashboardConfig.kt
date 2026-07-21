package com.project.integrationsdk.data

import android.content.Context

object DashboardConfig {

    private const val PREFS_NAME = "dashboard_prefs"
    private const val KEY_ACTIVE_ID = "active_dashboard_id"

    // ─────────────────────────────────────────────────────────────
    //  DASHBOARD LIST
    //  Populated from AndroidManifest.xml commented accounts.
    //  region is "eu1" for default (no CLEVERTAP_REGION needed).
    //  TO ADD A NEW DASHBOARD: append a Dashboard entry here.
    // ─────────────────────────────────────────────────────────────

    // identityKeys — used by CleverTapInstanceConfig.setIdentityKeys() to tell
    // the SDK which profile fields identify the user. Valid values: "Email",
    // "Phone", "Identity". Different dashboards may track identity differently,
    // so each entry below carries its own combination. Adjust per-dashboard as
    // needed; the placeholder "Email", "Identity" is a safe default.
    val dashboards = listOf(

        Dashboard(
            id = "karthik",
            name = "TEST — Karthik",
            accountId = "TEST-W8W-6WR-846Z",
            token = "TEST-206-0b0",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "karthik_test2",
            name = "TEST — KarthikTest2",
            accountId = "TEST-6Z4-46Z-776Z",
            token = "TEST-164-416",
            region = "eu1",
            identityKeys = arrayOf("Phone", "Identity")
        ),

        Dashboard(
            id = "karthik_test3",
            name = "TEST — KarthikTest3",
            accountId = "TEST-WW8-8RW-8Z7Z",
            token = "TEST-02b-b00",
            region = "eu1",
            identityKeys = arrayOf("Phone", "Identity")
        ),

        Dashboard(
            id = "karthik_test4",
            name = "TEST — KarthikTest4",
            accountId = "TEST-4R8-58W-7R7Z",
            token = "TEST-0b5-b24",
            region = "eu1",
            identityKeys = arrayOf("Phone", "Identity")
        ),

        Dashboard(
            id = "karthik_test5",
            name = "TEST — KarthikTest5",
            accountId = "TEST-869-958-K57Z",
            token = "TEST-b5c-c6b",
            region = "eu1",
            identityKeys = arrayOf("Phone", "Identity")
        ),

        Dashboard(
            id = "kk_test",
            name = "TEST — kkTest",
            accountId = "TEST-RK4-66R-966Z",
            token = "TEST-266-432",
            region = "eu1",
            identityKeys = arrayOf("Identity")
        ),

        Dashboard(
            id = "kk_central",
            name = "TEST — karthikCentral",
            accountId = "TEST-84R-5R9-967Z",
            token = "TEST-c25-24b",
            region = "eu1",
            identityKeys = arrayOf("Email","Identity")
        ),

        Dashboard(
            id = "rohit",
            name = "TEST — Rohit Khandka",
            accountId = "TEST-RZ5-677-767Z",
            token = "TEST-aa6-512",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "ecomm",
            name = "MultiApp — TESTECOMM",
            accountId = "58Z-5W6-866Z-TESTECOMM",
            token = "TEST-605-1b5",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "fintech",
            name = "MultiApp — TESTFINTECH",
            accountId = "58Z-5W6-866Z-TESTFINTECH",
            token = "TEST-605-1b5",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "jitendra",
            name = "JitendraClev",
            accountId = "65R-654-5Z6Z",
            token = "456-256",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "gaurav",
            name = "Gaurav — Android",
            accountId = "6ZR-965-446Z",
            token = "56c-216",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "sg1_bearded",
            name = "SG1 — Roaring Lion",
            accountId = "5WW-WWW-WW4Z",
            token = "000-005",
            region = "sg1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "in1_bearded",
            name = "IN1 — Bearded Robot",
            accountId = "ZWW-WWW-WW4Z",
            token = "000-001",
            region = "in1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "eu1_bearded",
            name = "EU1 — Bearded Robot",
            accountId = "ZWW-WWW-WWRZ",
            token = "000-001",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "eu1_android6",
            name = "EU1 — Android 6",
            accountId = "566-7Z4-W54Z",
            token = "41a-665",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "sk1_mobile_channel",
            name = "SK1 — Mobile Channels",
            accountId = "8KK-85K-996Z",
            token = "35b-33b",
            region = "sk1-staging-30",
            identityKeys = arrayOf("Email", "Identity")
        ),

        Dashboard(
            id = "TEST-Jude Migration",
            name = "TEST-Jude Migration",
            accountId = "R9Z-86K-Z57Z",
            token = "36b-1c2",
            region = "eu1",
            identityKeys = arrayOf("Email", "Identity")
        )

    )

    // ─────────────────────────────────────────────────────────────
    //  DATA CLASS
    //  region — "eu1" means default (no CLEVERTAP_REGION meta-data).
    //           "sg1" / "in1" / "eu1" etc for regional accounts.
    //  identityKeys — passed to CleverTapInstanceConfig.setIdentityKeys();
    //                 valid values "Email", "Phone", "Identity".
    // ─────────────────────────────────────────────────────────────

    data class Dashboard(
        val id: String,
        val name: String,
        val accountId: String,
        val token: String,
        val region: String? = "eu1",   // "eu1" = default region
        val identityKeys: Array<String> = arrayOf("Email", "Identity")
    )

    // ─────────────────────────────────────────────────────────────
    //  GET / SET ACTIVE
    // ─────────────────────────────────────────────────────────────

    fun getActive(context: Context): Dashboard {
        val savedId = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE_ID, dashboards.first().id)
        return dashboards.find { it.id == savedId } ?: dashboards.first()
    }

    fun setActive(context: Context, dashboard: Dashboard) {
        // commit() — write must be durable before SettingsFragment.restartApp()
        // calls Runtime.getRuntime().exit(0), otherwise an async apply() can be
        // dropped on process kill and the new selection is lost on restart.
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACTIVE_ID, dashboard.id)
            .commit()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}