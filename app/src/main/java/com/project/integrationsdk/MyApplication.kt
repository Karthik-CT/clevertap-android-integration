package com.project.integrationsdk

import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.data.CleverTapManager

class MyApplication : com.clevertap.android.sdk.Application() {

    override fun onCreate() {
        // Static CleverTap log level controls verbose/debug output across
        // BOTH static Logger.v/d calls AND per-instance logger.verbose/debug
        // (the instance methods route through getStaticDebugLevel internally).
        // Set it before anything the SDK might log during init.
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)

        super.onCreate()

        Log.i("MyApplication", "onCreate — CT static debugLevel=${CleverTapAPI.getDebugLevel()}")

        // Build the non-default instance for the active dashboard. setIdentityKeys
        // is a no-op on default instances, so we have to use this path.
        CleverTapManager.getInstance(this)

        CleverTapAPI.getDefaultInstance(applicationContext)?.initializeInbox()
    }
}