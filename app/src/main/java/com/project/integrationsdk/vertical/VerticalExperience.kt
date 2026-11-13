package com.project.integrationsdk.vertical

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.clevertap.android.sdk.CleverTapAPI
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.StateFlow

/** What a vertical's UI gets from the screen that hosts it. */
interface ExperienceHost {
    val context: Context
    val root: View
    val swipeRefresh: SwipeRefreshLayout
    val bottomNav: BottomNavigationView
    /** Container each vertical builds its screens into. */
    val content: LinearLayout
    val statusTop: Int
    val isDebug: Boolean
    val cleverTap: CleverTapAPI?
    fun refreshVariables(source: String)
    fun showMessage(message: String)
}

/**
 * One demo vertical's UI. The host (ProductExperiencesNewActivity) owns the lifecycle,
 * insets, pull-to-refresh and inbox; the experience owns its CleverTap variables,
 * theme, bottom-nav tabs and screens.
 */
interface VerticalExperience {
    /** Emits whenever the vertical's UI-driving values change. */
    val updates: StateFlow<Any>

    /** One-time setup: bottom-nav tabs etc. */
    fun setup()

    fun render(force: Boolean = false)
    fun fetch(onDone: (Boolean) -> Unit)
    fun updateInboxBadge()
}
