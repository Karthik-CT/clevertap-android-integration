package com.project.integrationsdk.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.databinding.ActivityProductExperiencesNewBinding
import com.project.integrationsdk.vertical.ExperienceHost
import com.project.integrationsdk.vertical.Vertical
import com.project.integrationsdk.vertical.VerticalExperience
import kotlinx.coroutines.launch

private const val PE_DEMO = "PE_DEMO"

/**
 * Host screen for every demo vertical. It owns the shared plumbing (insets, pull-to-refresh,
 * foreground refresh, inbox badge) and delegates the actual UI to the [VerticalExperience]
 * picked on VerticalPickerActivity.
 */
class ProductExperiencesNewActivity : AppCompatActivity(), CTInboxListener, ExperienceHost {

    private lateinit var binding: ActivityProductExperiencesNewBinding
    private lateinit var experience: VerticalExperience

    override val cleverTap: CleverTapAPI? get() = ct
    private var ct: CleverTapAPI? = null

    override var statusTop = 0
        private set

    override val isDebug get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    override val context: Context get() = this
    override val root: View get() = binding.root
    override val swipeRefresh: SwipeRefreshLayout get() = binding.swipeRefresh
    override val bottomNav: BottomNavigationView get() = binding.bottomNav
    override val content: LinearLayout get() = binding.content

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductExperiencesNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false

        ct = CleverTapManager.getInstance(applicationContext)
        val vertical = Vertical.fromName(intent.getStringExtra(Vertical.EXTRA_VERTICAL))
        experience = vertical.createExperience(this)

        setupInsets()
        setupInboxBadge()
        experience.setup()
        binding.swipeRefresh.setOnRefreshListener { refreshVariables("pull_to_refresh") }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                experience.updates.collect { experience.render() }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        experience.updateInboxBadge()
        refreshVariables("foreground")
    }

    // ── CleverTap: refresh triggers (foreground, pull-to-refresh, button) ──

    override fun refreshVariables(source: String) {
        Log.d(PE_DEMO, "fetchVariables requested from $source")
        experience.fetch { success ->
            Log.d(PE_DEMO, "fetchVariables callback ($source) success=$success")
            runOnUiThread { binding.swipeRefresh.isRefreshing = false }
        }
    }

    override fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .apply { if (binding.bottomNav.visibility == View.VISIBLE) setAnchorView(binding.bottomNav) }
            .show()
    }

    // ── Setup ──

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            if (top != statusTop) {
                statusTop = top
                experience.render(force = true)
            }
            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    // ── Inbox badge on the bell ──

    private fun setupInboxBadge() {
        ct?.apply {
            ctNotificationInboxListener = this@ProductExperiencesNewActivity
            initializeInbox()
        }
    }

    override fun inboxDidInitialize() { runOnUiThread { experience.updateInboxBadge() } }
    override fun inboxMessagesDidUpdate() { runOnUiThread { experience.updateInboxBadge() } }
}
