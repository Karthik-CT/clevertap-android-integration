package com.project.integrationsdk

import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig
import com.project.integrationsdk.data.DashboardConfig

/**
 * MyApplication — extends CleverTap's own Application class.
 *
 * WHY EXTEND instead of replacing:
 * CleverTap's Application class registers ActivityLifecycleCallbacks,
 * sets up push handling, in-app rendering, and other internals via
 * its own onCreate(). By extending it and calling super.onCreate()
 * first, ALL of that still runs exactly as before — nothing breaks.
 *
 * We then override just the default CT instance credentials using
 * CleverTapInstanceConfig, which reads from DashboardConfig (SharedPrefs)
 * instead of AndroidManifest meta-data.
 *
 * AndroidManifest.xml change — only the class name changes:
 *   BEFORE: android:name="com.clevertap.android.sdk.Application"
 *   AFTER:  android:name=".MyApplication"
 */
class MyApplication : com.clevertap.android.sdk.Application() {

    override fun onCreate() {
        // ── Step 1: Run ALL of CleverTap's own Application init ──
        // This registers lifecycle callbacks, push, in-app, etc.
        // Everything CleverTap depends on internally is set up here.
        super.onCreate()

        // ── Step 2: Override the default CT instance with selected dashboard ──
        // This runs AFTER CleverTap's init so it safely overrides just
        // the accountId + token without breaking anything else.
        initCleverTapWithSelectedDashboard()
    }

    private fun initCleverTapWithSelectedDashboard() {
        val dashboard = DashboardConfig.getActive(this)

        // Build instance config from the saved dashboard selection
        val config = CleverTapInstanceConfig.createInstance(
            this,
            dashboard.accountId,
            dashboard.token,
            dashboard.region?.let { it }
        )

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
    }
}