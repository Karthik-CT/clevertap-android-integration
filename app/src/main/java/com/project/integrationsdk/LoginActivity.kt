package com.project.integrationsdk

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.clevertap.android.pushtemplates.PTConstants
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig
import com.clevertap.android.sdk.Constants
import com.clevertap.android.sdk.inapp.CTLocalInApp
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.project.integrationsdk.databinding.ActivityLoginBinding
import java.math.BigInteger
import java.text.SimpleDateFormat
import kotlin.reflect.typeOf

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

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

        cleverTapDefaultInstance?.enableDeviceNetworkInfoReporting(true)

        binding.onUserLogin.setOnClickListener {
            onUserLogin()
        }

        binding.pushProfile.setOnClickListener {
            pushProfile()
        }

        binding.profileUploadBtn.setOnClickListener {
            uploadPofileTest()
        }
    }

    override fun onResume() {
        super.onResume()

        val builder = CTLocalInApp.builder()
            .setInAppType(CTLocalInApp.InAppType.ALERT)
            .setTitleText("Get Notified")
            .setMessageText("Enable Notification permission")
            .followDeviceOrientation(true)
            .setPositiveBtnText("Allow")
            .setNegativeBtnText("Cancel")
            .build()
        cleverTapDefaultInstance?.promptPushPrimer(builder)

//        cleverTapDefaultInstance?.promptForPushPermission(false)
    }


    private fun onUserLogin() {
//        val location = cleverTapDefaultInstance!!.location
//        cleverTapDefaultInstance!!.location = location
//        println("Location $location")
//        Log.d("location", "Latitude: ${location.latitude} longitude: ${location.longitude}")

//        val arList = listOf("test101", "test102", "test105", "test106")
//        for (x in arList) {
//            if (binding.userIdentity.text.toString() == x) {
//                val profile = HashMap<String, Any>()
//                profile["Name"] = binding.userName.text.toString()
//                profile["Identity"] = binding.userIdentity.text.toString()
//                profile["Email"] = binding.emailId.text.toString()
//                profile["Phone"] = binding.mobileNo.text.toString()
//                profile["MSG-email"] = false
//                profile["MSG-push"] = false
//                profile["MSG-sms"] = false
//                profile["MSG-whatsapp"] = false
//                profile["signup_date"] = SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022")
//                profile["DOB"] = SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022")
//
//                println("$x and ${binding.userIdentity.text.toString()} if part")
//
//                CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profile)
//                startActivity(Intent(applicationContext, MainActivity::class.java))
//                finish()
//                Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT).show()
//            } else {
//                val profile = HashMap<String, Any>()
//                profile["Name"] = binding.userName.text.toString()
//                profile["Identity"] = binding.userIdentity.text.toString()
//                profile["Email"] = binding.emailId.text.toString()
//                profile["Phone"] = binding.mobileNo.text.toString()
//                profile["MSG-email"] = true
//                profile["MSG-push"] = true
//                profile["MSG-sms"] = true
//                profile["MSG-whatsapp"] = true
//                profile["signup_date"] = SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022")
//                profile["DOB"] = SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022")
//
//                println("$x and ${binding.userIdentity.text.toString()} else part")
//
//                CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profile)
//                startActivity(Intent(applicationContext, MainActivity::class.java))
//                finish()
//                Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT).show()
//            }
//        }

//        val arrInt = ArrayList<BigInteger>()
//        arrInt.add(1999)
//        arrInt.add(2999)
//        arrInt.add(3999)
//        arrInt.add(4999)
//        println("ArrInt Value: $arrInt")

        val profile = HashMap<String, Any>()
//        profile["total_cart_values"] = arrInt
        profile["Name"] = binding.userName.text.toString()
        profile["Identity"] = binding.userIdentity.text.toString()
        profile["Email"] = binding.emailId.text.toString()
        profile["Phone"] = binding.mobileNo.text.toString()
        profile["MSG-email"] = true
        profile["MSG-push"] = true
        profile["MSG-sms"] = true
        profile["MSG-whatsapp"] = true
        profile["signup_date"] = SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022")
        profile["DOB"] = SimpleDateFormat("MMM dd, yyyy").parse("Feb 15, 2022")


//        profile["latitude"] = location.latitude
//        profile["longitude"] = location.longitude

        profile["items_to_recommend"] = arrayListOf("CT000001", "CT000002", "CT000003", "CT000004", "CT000005")
        profile["int_values"] = intArrayOf(19,29,39,49)
        CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profile)
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT).show()
    }

    private fun pushProfile() {
//        val location = cleverTapDefaultInstance!!.location
//        cleverTapDefaultInstance!!.location = location
//        Log.d("location", "Latitude: ${location.latitude} longitude: ${location.longitude}")


        val profile = HashMap<String, Any>()
        profile["Name"] = binding.userName.text.toString()
        profile["Identity"] = binding.userIdentity.text.toString()
        profile["Email"] = binding.emailId.text.toString()
        profile["Phone"] = binding.mobileNo.text.toString()
        profile["MSG-email"] = true
        profile["MSG-push"] = true
        profile["MSG-sms"] = true
        profile["MSG-whatsapp"] = true
//        profile["latitude"] = location.latitude
//        profile["longitude"] = location.longitude

        CleverTapAPI.getDefaultInstance(applicationContext)?.pushProfile(profile)
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Profile Pushed!", Toast.LENGTH_SHORT).show()
    }

    private fun uploadPofileTest(){
        val profile = HashMap<String, Any>()
        profile["Name"] = binding.userName.text.toString()
        profile["Identity"] = "el1"
        profile["Identity"] = binding.userIdentity.text.toString()
        profile["Email"] = binding.emailId.text.toString()
        profile["Phone"] = binding.mobileNo.text.toString()
        profile["MSG-email"] = true
        profile["MSG-push"] = true
        profile["MSG-sms"] = true
        profile["MSG-whatsapp"] = true

        CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profile)

        Toast.makeText(applicationContext, "uploadPofileTest() Pushed!", Toast.LENGTH_SHORT).show()
    }

    fun dismissNotification(intent: Intent?, applicationContext: Context){
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
            val ptDismissOnClick = intent.extras!!.getString(PTConstants.PT_DISMISS_ON_CLICK,"")

            if (autoCancel && notificationId > -1 && ptDismissOnClick.isNullOrEmpty()) {
                val notifyMgr: NotificationManager =
                    applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notifyMgr.cancel(notificationId)
            }
        }
    }
}