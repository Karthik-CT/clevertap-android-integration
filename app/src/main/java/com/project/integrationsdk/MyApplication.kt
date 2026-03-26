package com.project.integrationsdk

import com.project.integrationsdk.utils.CleverTapManager
import com.clevertap.android.sdk.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CleverTapManager.init(this)
    }
}