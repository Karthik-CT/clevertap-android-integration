package com.project.integrationsdk.ui

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.clevertap.android.pushtemplates.PTConstants
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.clevertap.android.sdk.login.LoginInfoProvider
import com.project.integrationsdk.MainActivity
import com.project.integrationsdk.databinding.ActivityLoginBinding
import com.project.integrationsdk.session.SessionManager
import org.json.JSONObject
import java.text.SimpleDateFormat

class LoginActivity : AppCompatActivity(), PushPermissionResponseListener {

    lateinit var binding: ActivityLoginBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        if (SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(binding.root)

//        val config  = CleverTapInstanceConfig.getDefaultInstance(applicationContext)
//        LoginInfoProvider(applicationContext, config).saveIdentityKeysForAccount("Identity,Phone")
//
//        val wizRocketPrefs = getSharedPreferences("WizRocket", Context.MODE_PRIVATE)
//        val key = "SP_KEY_PROFILE_IDENTITIES:TEST-6Z4-46Z-776Z"
//        val currentValue = wizRocketPrefs.getString(key, "") ?: ""
//        if (!currentValue.contains("Identity")) {
//            val newValue = "Identity,Phone"
//            wizRocketPrefs.edit().putString(key, newValue).apply()
//        }

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        cleverTapDefaultInstance?.registerPushPermissionNotificationResponseListener(this)
        CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler() as NotificationHandler);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CleverTapAPI.createNotificationChannelGroup(
                applicationContext,
                "testkk1",
                "Notification Test"
            )
        }
        CleverTapAPI.createNotificationChannel(
            applicationContext, "testkk123", "Notification Test", "CleverTap Notification Test",
            NotificationManager.IMPORTANCE_MAX, true
        )
        CleverTapAPI.createNotificationChannel(
            applicationContext,
            "testkk1234",
            "KK Notification Test",
            "KK CleverTap Notification Test",
            NotificationManager.IMPORTANCE_MAX,
            true
        )

        CleverTapAPI.createNotificationChannel(
            getApplicationContext(),
            "channelSound",
            "channelSound",
            "channelSound",
            NotificationManager.IMPORTANCE_MAX,
            true,
            "channelsound1.wav"
        )
//        android:value="appid=101300553"

        cleverTapDefaultInstance?.enableDeviceNetworkInfoReporting(true)

        binding.onUserLogin.setOnClickListener {
            onUserLogin()
        }

        binding.pushProfile.setOnClickListener {
            pushProfile()
        }

//        printSharedPreferences(applicationContext)
//        clearIdentityErrorIssue(applicationContext)

//        cleverTapDefaultInstance?.promptForPushPermission(true)

    }

    override fun onResume() {
        super.onResume()

//        if (cleverTapDefaultInstance!!.isPushPermissionGranted()) {
//            Log.d("CT", "onResume: ALready granted")
//        } else {
//            val builder = CTLocalInApp.builder()
//                .setInAppType(CTLocalInApp.InAppType.ALERT)
//                .setTitleText("Get Notified")
//                .setMessageText("Enable Notification permission")
//                .followDeviceOrientation(true)
//                .setPositiveBtnText("Allow")
//                .setNegativeBtnText("Cancel")
//                .build()
//            cleverTapDefaultInstance?.promptPushPrimer(builder)
        //  }

        cleverTapDefaultInstance?.promptForPushPermission(true)

//        val builder = CTLocalInApp.builder()
//            .setInAppType(CTLocalInApp.InAppType.ALERT)
//            .setTitleText("Get Notified")
//            .setMessageText("Enable Notification permission")
//            .followDeviceOrientation(true)
//            .setPositiveBtnText("Allow")
//            .setNegativeBtnText("Cancel")
//            .build()
//        cleverTapDefaultInstance?.promptPushPrimer(builder)

//        cleverTapDefaultInstance?.promptForPushPermission(false)


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

    object NotificationUtils {

        //Require to close notification on action button click
        fun dismissNotification(intent: Intent?, applicationContext: Context) {
            intent?.extras?.apply {
                var autoCancel = true
                var notificationId = -1

                getString("actionId")?.let {
                    Log.d("ACTION_ID", it)
                    autoCancel = getBoolean("autoCancel", true)
                    notificationId = getInt("notificationId", -1)
                }
                /**
                 * If using InputBox template, add ptDismissOnClick flag to not dismiss notification
                 * if pt_dismiss_on_click is false in InputBox template payload. Alternatively if normal
                 * notification is raised then we dismiss notification.
                 */
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


    private fun onUserLogin(): Unit = with(binding) {
        val name = userName.text.toString().trim()
        val identity = userIdentity.text.toString().trim()
        val email = emailId.text.toString().trim()
        val mobile = mobileNo.text.toString().trim()
        if (identity.isBlank() || email.isBlank()) {
            Toast.makeText(this@LoginActivity, "Identity and Email are required", Toast.LENGTH_SHORT).show()
            return
        }
        val profile: MutableMap<String, Any> = hashMapOf(
            "Identity" to identity,
            "Email" to email,
            "MSG-email" to true,
            "MSG-push" to true,
            "MSG-sms" to true,
            "MSG-whatsapp" to true,
            "signup_date" to SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022"),
            "DOB" to SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022"),
            "items_to_recommend" to arrayListOf("CT000001","CT000002","CT000003","CT000004","CT000005"),
            "int_values" to intArrayOf(19, 29, 39, 49)
        )
        mobile.takeIf { it.isNotBlank() }?.let { profile["Phone"] = "+$it" }
        name.takeIf { it.isNotBlank() }?.let { profile["Name"] = it }
        CleverTapAPI.getDefaultInstance(this@LoginActivity)?.onUserLogin(profile)
        SessionManager.login(applicationContext, identity, email, if (mobile.isNotBlank()) "+$mobile" else "", name)
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT).show()
    }

    private fun pushProfile(): Unit = with(binding) {
        val name = userName.text.toString().trim()
        val identity = userIdentity.text.toString().trim()
        val email = emailId.text.toString().trim()
        val mobile = mobileNo.text.toString().trim()
        if (identity.isBlank() || email.isBlank()) {
            Toast.makeText(this@LoginActivity, "Identity and Email are required", Toast.LENGTH_SHORT).show()
            return
        }
        val profile: MutableMap<String, Any> = hashMapOf(
            "Identity" to identity,
            "Email" to email,
            "MSG-email" to true,
            "MSG-push" to true,
            "MSG-sms" to true,
            "MSG-whatsapp" to true
        )
        mobile.takeIf { it.isNotBlank() }?.let { profile["Phone"] = "+$it" }
        name.takeIf { it.isNotBlank() }?.let { profile["Name"] = it }
        CleverTapAPI.getDefaultInstance(applicationContext)?.pushProfile(profile)
        SessionManager.login(applicationContext, identity, email, if (mobile.isNotBlank()) "+$mobile" else "", name)
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Profile Pushed!", Toast.LENGTH_SHORT).show()
    }

    fun dismissNotification(intent: Intent?, applicationContext: Context) {
        intent?.extras?.apply {
            var autoCancel = true
            var notificationId = -1

            getString("actionId")?.let {
                Log.d("ACTION_ID", it)
                autoCancel = getBoolean("autoCancel", true)
                notificationId = getInt("notificationId", -1)
            }
            /**
             * If using InputBox template, add ptDismissOnClick flag to not dismiss notification
             * if pt_dismiss_on_click is false in InputBox template payload. Alternatively if normal
             * notification is raised then we dismiss notification.
             */
            val ptDismissOnClick = intent.extras!!.getString(PTConstants.PT_DISMISS_ON_CLICK, "")

            if (autoCancel && notificationId > -1 && ptDismissOnClick.isNullOrEmpty()) {
                val notifyMgr: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifyMgr.cancel(notificationId)
            }
        }
    }

    override fun onPushPermissionResponse(accepted: Boolean) {
        Log.d("CT", "onPushPermissionResponse: ")
    }

    fun printSharedPreferences(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        for ((key, value) in prefs.all) {
            Log.d("UserPrefs", "SharedPref: $key = $value")
        }
    }


    fun clearIdentityErrorIssue(context: Context) {
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
}