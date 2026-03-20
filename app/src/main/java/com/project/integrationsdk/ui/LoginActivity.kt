package com.project.integrationsdk.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.pushtemplates.PTConstants
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.project.integrationsdk.MainActivity
import com.project.integrationsdk.databinding.ActivityLoginBinding
import com.project.integrationsdk.session.SessionManager
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity(), PushPermissionResponseListener {

    private lateinit var binding: ActivityLoginBinding
    private val formatter by lazy {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }
    private val ctInstance by lazy {
        CleverTapAPI.getDefaultInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        setContentView(binding.root)

//        val config  = CleverTapInstanceConfig.getDefaultInstance(applicationContext)
//        LoginInfoProvider(applicationContext, config).saveIdentityKeysForAccount("Identity,Phone")
//        val wizRocketPrefs = getSharedPreferences("WizRocket", Context.MODE_PRIVATE)
//        val key = "SP_KEY_PROFILE_IDENTITIES:TEST-6Z4-46Z-776Z"
//        val currentValue = wizRocketPrefs.getString(key, "") ?: ""
//        if (!currentValue.contains("Identity")) {
//            val newValue = "Identity,Phone"
//            wizRocketPrefs.edit().putString(key, newValue).apply()
//        }

        binding.onUserLogin.setOnClickListener {
            onUserLogin()
        }

        binding.pushProfile.setOnClickListener {
            pushProfile()
        }

//        printSharedPreferences(applicationContext)
//        clearIdentityErrorIssue(applicationContext)
    }

    override fun onResume() {
        super.onResume()

        ctInstance?.promptForPushPermission(true)

        val payload = this.intent?.extras
        println("PT Payload: $payload")
        if (payload?.containsKey("pt_id") == true && payload["pt_id"] == "pt_rating") {
            val nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(payload["notificationId"] as Int)
        }
        if (payload?.containsKey("pt_id") == true && payload["pt_id"] == "pt_product_display") {
            val nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(payload["notificationId"] as Int)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            NotificationUtils.dismissNotification(intent, applicationContext)
        }
    }

    override fun onPushPermissionResponse(accepted: Boolean) {
        Log.d("CT", "onPushPermissionResponse: $accepted")
    }

    private fun onUserLogin() = handleUserAction(true,"Logged in!") { profile ->
        ctInstance?.onUserLogin(profile)
    }

    private fun pushProfile() = handleUserAction(true, "Profile Pushed!") { profile ->
        ctInstance?.pushProfile(profile)
    }

    private inline fun handleUserAction(isLogin: Boolean, successMessage: String, action: (MutableMap<String, Any>) -> Unit) = with(binding) {
        val identity = userIdentity.text.toString().trim()
        val email = emailId.text.toString().trim()
        val name = userName.text.toString().trim()
        val mobile = mobileNo.text.toString().trim()

        // Only Identity & Email mandatory
        if (identity.isBlank() || email.isBlank()) {
            Toast.makeText(this@LoginActivity, "Identity and Email are required", Toast.LENGTH_SHORT).show()
            return
        }

        val profile = buildMap{
            put("Identity", identity)
            put("Email", email)
            put("MSG-email", true)
            put("MSG-push", true)
            put("MSG-sms", true)
            put("MSG-whatsapp", true)
            if (isLogin) {
                put("signup_date", formatter.parse("Feb 15, 2022")!!)
                put("DOB", formatter.parse("Feb 15, 2022")!!)
                put(
                    "items_to_recommend",
                    listOf("CT000001", "CT000002", "CT000003", "CT000004", "CT000005")
                )
                put("int_values", intArrayOf(19, 29, 39, 49))
            }
            name.takeIf { it.isNotBlank() }?.let { put("Name", it) }
            mobile.takeIf { it.isNotBlank() }?.let { put("Phone", "+$it") }
        }.toMutableMap()

        // Call CleverTap action
        action(profile)

        // Save session only on login
        if (isLogin) {
            SessionManager.login( this@LoginActivity, identity, email, mobile.takeIf { it.isNotBlank() }?.let { "+$it" } ?: "", name)
            navigateToHome()
        }
        Toast.makeText(this@LoginActivity, successMessage, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun dismissNotification(intent: Intent?, applicationContext: Context) {
        intent?.extras?.apply {
            var autoCancel = true
            var notificationId = -1

            getString("actionId")?.let {
                Log.d("ACTION_ID", it)
                autoCancel = getBoolean("autoCancel", true)
                notificationId = getInt("notificationId", -1)
            }
            val ptDismissOnClick = intent.extras!!.getString(PTConstants.PT_DISMISS_ON_CLICK, "")

            if (autoCancel && notificationId > -1 && ptDismissOnClick.isNullOrEmpty()) {
                val notifyMgr: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifyMgr.cancel(notificationId)
            }
        }
    }

    private fun printSharedPreferences(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        for ((key, value) in prefs.all) {
            Log.d("UserPrefs", "SharedPref: $key = $value")
        }
    }

    private fun clearIdentityErrorIssue(context: Context) {
        val prefs = context.getSharedPreferences("WizRocket", Context.MODE_PRIVATE)
        val cachedGuidsEntry = prefs.all.entries.firstOrNull {
            it.key.startsWith("cachedGUIDsKey:")
        }

        if (cachedGuidsEntry == null || cachedGuidsEntry.value !is String) {
            Log.i("CT_FIX", "No CachedGUIDS found")
            return
        }

        val cachedGuidsJson = cachedGuidsEntry.value as String
        Log.d("CT_FIX", "Cached GUIDS JSON: $cachedGuidsJson")

        val identityIDs = mutableListOf<String>()
        val emailIDs = mutableListOf<String>()

        try {
            val jsonObject = JSONObject(cachedGuidsJson)
            jsonObject.keys().forEach { key ->
                val ctId = jsonObject.getString(key)
                when {
                    key.startsWith("Identity_") -> identityIDs.add(ctId)
                    key.startsWith("Email_") -> emailIDs.add(ctId)
                }
            }
        } catch (e: Exception) {
            Log.e("CT_FIX", "Failed to parse CachedGUIDS JSON", e)
            return
        }

        var shouldClear = false

        if (identityIDs.size != identityIDs.toSet().size) {
            Log.w("CT_FIX", "Duplicate Identity_ CleverTap IDs found")
            shouldClear = true
        }

        if (emailIDs.size != emailIDs.toSet().size) {
            Log.w("CT_FIX", "Duplicate Email_ CleverTap IDs found")
            shouldClear = true
        }
        if (shouldClear) {
            Log.w("CT_FIX", "Duplicates detected. Clearing wizrocket SharedPreferences")

            prefs.edit().clear().apply()

            Log.i("CT_FIX", "wizrocket SharedPreferences cleared")
        } else {
            Log.i("CT_FIX", "No duplicates found. CachedGUIDS retained")
        }
    }

    object NotificationUtils {
        fun dismissNotification(intent: Intent?, applicationContext: Context) {
            intent?.extras?.apply {
                var autoCancel = true
                var notificationId = -1

                getString("actionId")?.let {
                    Log.d("ACTION_ID", it)
                    autoCancel = getBoolean("autoCancel", true)
                    notificationId = getInt("notificationId", -1)
                }
                val ptDismissOnClick =
                    intent.extras!!.getString(PTConstants.PT_DISMISS_ON_CLICK, "")

                if (autoCancel && notificationId > -1 && ptDismissOnClick.isNullOrEmpty()) {
                    val notifyMgr: NotificationManager =
                        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notifyMgr.cancel(notificationId)
                }
            }
        }
    }

}