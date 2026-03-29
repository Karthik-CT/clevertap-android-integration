package com.project.integrationsdk.ui

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.clevertap.android.pushtemplates.PTConstants
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.project.integrationsdk.utils.CleverTapIdManager
import com.project.integrationsdk.MainActivity
import com.project.integrationsdk.databinding.ActivityLoginBinding
import com.project.integrationsdk.utils.CleverTapManager

class LoginActivity : AppCompatActivity(), PushPermissionResponseListener {

    lateinit var binding: ActivityLoginBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        CleverTapManager.pushEvent("testEvent")
//        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext, CleverTapIdManager.generateId())

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

        cleverTapDefaultInstance?.enableDeviceNetworkInfoReporting(true)

        binding.onUserLogin.setOnClickListener {
            doOnUserLogin()
        }

        binding.signUp.setOnClickListener {
            doSignUp()
        }

    }

    override fun onResume() {
        super.onResume()

        cleverTapDefaultInstance?.promptForPushPermission(true)

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

    private fun doOnUserLogin() {
        val identity = binding.userIdentity.text.toString().trim()

        val profile = HashMap<String, Any>()
        profile["Name"] = binding.userName.text.toString()
        profile["Identity"] = identity
        profile["Email"] = binding.emailId.text.toString()
        profile["Phone"] = "+" + binding.mobileNo.text.toString()
        profile["MSG-email"] = true
        profile["MSG-push"] = true
        profile["MSG-sms"] = true
        profile["MSG-whatsapp"] = true

        CleverTapManager.onUserLogin(profile, identity, applicationContext)

        startActivity(
            Intent(applicationContext, MainActivity::class.java).apply {
                putExtra("Identity", identity)
                putExtra("Email", binding.emailId.text.toString())
            }
        )
        finish()
        Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT).show()
    }

    private fun doSignUp() {
        val identity = binding.userIdentity.text.toString().trim()

        val profile = HashMap<String, Any>()
        profile["Name"] = binding.userName.text.toString()
        profile["Identity"] = identity
        profile["Email"] = binding.emailId.text.toString()
        profile["Phone"] = "+" + binding.mobileNo.text.toString()
        profile["MSG-email"] = true
        profile["MSG-push"] = true
        profile["MSG-sms"] = true
        profile["MSG-whatsapp"] = true

        if (CleverTapIdManager.isFirstTimeSignup(applicationContext)) {
            CleverTapManager.onFirstSignup(profile, identity, applicationContext)
        } else {
            CleverTapManager.onUserLogin(profile, identity, applicationContext)
        }

        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Signed up!", Toast.LENGTH_SHORT).show()
    }

    override fun onPushPermissionResponse(accepted: Boolean) {
        Log.d("CT", "onPushPermissionResponse: ")
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
}