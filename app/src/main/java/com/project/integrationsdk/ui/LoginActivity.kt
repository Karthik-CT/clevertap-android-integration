package com.project.integrationsdk.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.clevertap.android.pushtemplates.PTConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.project.integrationsdk.MainActivity
import com.project.integrationsdk.data.CleverTapHelper
import com.project.integrationsdk.data.CleverTapManager
import com.project.integrationsdk.data.DashboardConfig
import com.project.integrationsdk.data.UserPrefs
import com.project.integrationsdk.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), PushPermissionResponseListener {

    private lateinit var binding: ActivityLoginBinding
    private val ct by lazy { CleverTapManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // UserPrefs is namespaced by the active DashboardConfig id, so this
        // flag is per-dashboard: signed in on Karthik stays signed in only
        // on Karthik; switching to a dashboard the user has never signed
        // in on lands them back on this Login screen.
        val activeDashboard = DashboardConfig.getActive(this)
        val signedIn = UserPrefs.isLoggedIn(this)
        Log.i(
            "LoginActivity",
            "Active dashboard=${activeDashboard.name} signedIn=$signedIn"
        )
        if (signedIn) {
            goHome()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.heroContainer) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val extra = (16 * resources.displayMetrics.density).toInt()
            v.setPadding(v.paddingLeft, top + extra, v.paddingRight, v.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(binding.heroContainer)

        binding.onUserLogin.setOnClickListener { view ->
            view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
                view.animate().scaleX(1f).scaleY(1f)
                    .setDuration(150).setInterpolator(DecelerateInterpolator()).start()
            }.start()
            onLoginSuccess()
        }

        binding.pushProfile.setOnClickListener { view ->
            view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction {
                view.animate().scaleX(1f).scaleY(1f)
                    .setDuration(150).setInterpolator(DecelerateInterpolator()).start()
            }.start()
            pushProfileOnly()
        }
    }

    override fun onResume() {
        super.onResume()
        ct?.promptForPushPermission(true)
        val payload = intent?.extras
        if (payload?.containsKey("pt_id") == true) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(payload["notificationId"] as? Int ?: 0)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationUtils.dismissNotification(intent, applicationContext)
        }
    }

    override fun onPushPermissionResponse(accepted: Boolean) {
        Log.d("CT", "Push permission: $accepted")
    }

    private fun onLoginSuccess() {
        val name     = binding.userName.text.toString().trim()
        val phone    = binding.mobileNo.text.toString().trim()
        val identity = binding.userIdentity.text.toString().trim()
        val email    = binding.emailId.text.toString().trim()

        if (identity.isEmpty() || email.isEmpty()) {
            toast("Identity and Email are required")
            return
        }

        val nameParts = name.split(" ", limit = 2)
        val profile = UserPrefs.Profile(
            firstName = nameParts.getOrElse(0) { "" },
            lastName  = nameParts.getOrElse(1) { "" },
            email     = email,
            phone     = if (phone.isEmpty()) "" else if (phone.startsWith("+")) phone else "+$phone",
            identity  = identity
        )

        // login() sets is_logged_in = true AND saves profile — one call does both
        UserPrefs.login(this, profile)

        // Identify user in CleverTap
        CleverTapHelper.onLogin(ct, profile)

        toast("Signed in!")
        goHome()
    }

    private fun pushProfileOnly() {
        val name     = binding.userName.text.toString().trim()
        val phone    = binding.mobileNo.text.toString().trim()
        val email    = binding.emailId.text.toString().trim()
        val identity = binding.userIdentity.text.toString().trim()

        if (identity.isEmpty() || email.isEmpty()) {
            toast("Identity and Email are required")
            return
        }

        val nameParts = name.split(" ", limit = 2)
        val profile = UserPrefs.Profile(
            firstName = nameParts.getOrElse(0) { "" },
            lastName  = nameParts.getOrElse(1) { "" },
            email     = email,
            phone     = if (phone.startsWith("+")) phone else "+$phone",
            identity  = identity
        )

        CleverTapHelper.updateProfile(ct, profile)
        toast("Profile pushed!")
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    object NotificationUtils {
        fun dismissNotification(intent: Intent?, context: Context) {
            intent?.extras?.apply {
                var autoCancel = true
                var notificationId = -1
                getString("actionId")?.let {
                    autoCancel = getBoolean("autoCancel", true)
                    notificationId = getInt("notificationId", -1)
                }
                val ptDismiss = intent.extras?.getString(PTConstants.PT_DISMISS_ON_CLICK, "") ?: ""
                if (autoCancel && notificationId > -1 && ptDismiss.isEmpty()) {
                    (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                        .cancel(notificationId)
                }
            }
        }
    }
}