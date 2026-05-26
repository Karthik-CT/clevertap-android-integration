package com.project.integrationsdk.data

import android.content.Context
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig

/**
 * Builds and returns the CleverTap instance for the dashboard the user
 * selected in Settings. Uses CleverTapInstanceConfig.createInstance() +
 * setIdentityKeys() (which only takes effect on a NON-default instance)
 * so each dashboard can declare its own identity-key combination — e.g.
 * one account on Email+Identity, another on Identity-only, another on
 * Email+Phone+Identity. The SDK's instanceWithConfig() caches by
 * accountId so repeated calls are cheap.
 *
 * Callers should use CleverTapManager.getInstance(context) anywhere they
 * previously used CleverTapAPI.getDefaultInstance(context).
 */
object CleverTapManager {

    private const val TAG = "CleverTapManager"

    fun getInstance(context: Context): CleverTapAPI? {
        // Re-assert static debug level — verbose/debug from BOTH the static
        // Logger.v/d helpers AND the per-instance logger.verbose/debug methods
        // gates on CleverTapAPI.getDebugLevel(). Setting it here too means
        // any caller (Activity/Fragment) hitting getInstance() before
        // MyApplication.onCreate runs still gets verbose output.
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)

        val dashboard = DashboardConfig.getActive(context)
        val config = CleverTapInstanceConfig.createInstance(
            context,
            dashboard.accountId,
            dashboard.token,
            dashboard.region
        ) ?: return null

        // CleverTapInstanceConfig's constructor hard-codes the per-instance
        // debug level to INFO. The per-instance logger.info(...) check uses
        // this field directly, so raise it before any logging happens.
        config.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        config.setIdentityKeys(*dashboard.identityKeys)
        Log.i(
            TAG,
            "Using CleverTap dashboard=${dashboard.name} " +
                "accountId=${dashboard.accountId} region=${dashboard.region} " +
                "identityKeys=${dashboard.identityKeys.joinToString()} " +
                "configDebugLevel=${config.debugLevel} " +
                "staticDebugLevel=${CleverTapAPI.getDebugLevel()}"
        )
        return CleverTapAPI.instanceWithConfig(context, config)
    }
}
