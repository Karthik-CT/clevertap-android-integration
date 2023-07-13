package com.project.integrationsdk

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.CleverTapInstanceConfig
import com.project.integrationsdk.databinding.ActivityLoginBinding
import java.text.SimpleDateFormat

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

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

        val profile = HashMap<String, Any>()
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
        CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profile)
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
        Toast.makeText(applicationContext, "Logged in!", Toast.LENGTH_SHORT).show()
    }

    private fun pushProfile() {
        val location = cleverTapDefaultInstance!!.location
        cleverTapDefaultInstance!!.location = location
        Log.d("location", "Latitude: ${location.latitude} longitude: ${location.longitude}")


        val profile = HashMap<String, Any>()
        profile["Name"] = binding.userName.text.toString()
        profile["Identity"] = binding.userIdentity.text.toString()
        profile["Email"] = binding.emailId.text.toString()
        profile["Phone"] = binding.mobileNo.text.toString()
        profile["MSG-email"] = true
        profile["MSG-push"] = true
        profile["MSG-sms"] = true
        profile["MSG-whatsapp"] = true
        profile["latitude"] = location.latitude
        profile["longitude"] = location.longitude

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
}