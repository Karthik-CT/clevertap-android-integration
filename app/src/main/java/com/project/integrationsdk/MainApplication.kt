package com.project.integrationsdk

import android.annotation.SuppressLint
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.Application
import com.clevertap.android.sdk.CleverTapAPI.*

class MainApplication: Application() {
    @SuppressLint("MParticleInitialization")
    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        setDebugLevel(LogLevel.DEBUG)
    }

}