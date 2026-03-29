package com.project.integrationsdk

import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.project.integrationsdk.utils.CleverTapManager
import com.clevertap.android.sdk.Application
import com.clevertap.android.sdk.CleverTapAPI

class MyApplication : Application() {

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        CleverTapManager.init(this)
    }
}