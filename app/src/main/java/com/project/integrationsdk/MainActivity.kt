package com.project.integrationsdk

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clevertap.android.geofence.CTGeofenceAPI
import com.clevertap.android.geofence.CTGeofenceSettings
import com.clevertap.android.geofence.Logger
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.project.integrationsdk.databinding.ActivityMainBinding
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.location.LocationManager

class MainActivity : AppCompatActivity(), CTInboxListener {

    lateinit var binding: ActivityMainBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null
    var ctGeofenceAPI : CTGeofenceAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        ctGeofenceAPI = CTGeofenceAPI.getInstance(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CleverTapAPI.createNotificationChannelGroup(applicationContext, "testkk1", "Notification Test")
        }
        CleverTapAPI.createNotificationChannel(
            applicationContext, "testkk123", "Notification Test", "CleverTap Notification Test",
            NotificationManager.IMPORTANCE_MAX, true
        )

//        addUserDetails1()
        addUserDetails()

        binding.updateProfile.setOnClickListener {
//            addUserDetails1()
        addUserDetails()
            Toast.makeText(applicationContext, "Profile Updated!", Toast.LENGTH_SHORT).show()
        }
        binding.addEvents.setOnClickListener {
            addEvents()
            Toast.makeText(applicationContext, "Events button Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.pushNotification.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Noti Event")
            Toast.makeText(applicationContext, "Notification button Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.inapp.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's InApp Event")
            Toast.makeText(applicationContext, "InApp button Clicked", Toast.LENGTH_SHORT).show()
        }
        cleverTapDefaultInstance?.apply {
            ctNotificationInboxListener = this@MainActivity
            initializeInbox()
        }
        binding.nativeDisplay.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Native Display Event")
            startActivity(Intent(applicationContext, NativeDisplayActivity::class.java))
            Toast.makeText(applicationContext, "Native Display button Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.geofence.setOnClickListener {
            startActivity(Intent(applicationContext, GeofenceActivity::class.java))
            Toast.makeText(applicationContext, "Geofence button Clicked", Toast.LENGTH_SHORT).show()
        }

        //GeoFence
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "Please grant the Permission", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
            geoFencing()
        }

        //custom app inbox
        binding.customAppInbox.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's App Inbox Event")
            Toast.makeText(applicationContext, "Custom App Inbox button Clicked", Toast.LENGTH_SHORT).show()
            startActivity(Intent(applicationContext, CustomAppInboxActivity::class.java))
        }

        //product experience

    }

    fun geoFencing() {
        val ctGeofenceSettings = CTGeofenceSettings.Builder()
            .enableBackgroundLocationUpdates(true)//boolean to enable background location updates
            .setLogLevel(Logger.DEBUG)//Log Level
            .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)//byte value for Location Accuracy
            .setLocationFetchMode(CTGeofenceSettings.FETCH_LAST_LOCATION_PERIODIC)//byte value for Fetch Mode
            .setGeofenceMonitoringCount(50)//int value for number of Geofences CleverTap can monitor
            .setInterval(1800000)//long value for interval in milliseconds
            .setFastestInterval(1800000)//long value for fastest interval in milliseconds
            .setSmallestDisplacement(200F)//float value for smallest Displacement in meters
            .setGeofenceNotificationResponsiveness(0)// int value for geofence notification responsiveness in milliseconds
            .build()

        ctGeofenceAPI?.init(ctGeofenceSettings, cleverTapDefaultInstance!!)

        try {
            CTGeofenceAPI.getInstance(applicationContext).triggerLocation()
        } catch (e: IllegalStateException) { // thrown when this method is called before geofence SDK initialization
            e.printStackTrace()
        }

        //callbacks
        CTGeofenceAPI.getInstance(applicationContext).setOnGeofenceApiInitializedListener {
            //App is notified on the main thread that CTGeofenceAPI is initialized
            Log.d("clevertap_geofence", "CTGeofenceAPI is initialized")
            println("CTGeofenceAPI is initialized called")
        }

        CTGeofenceAPI.getInstance(applicationContext)
            .setCtGeofenceEventsListener(object : CTGeofenceEventsListener {
                override fun onGeofenceEnteredEvent(jsonObject: JSONObject) {
                    //Callback on the main thread when the user enters Geofence with info in jsonObject
                    Log.d("clevertap_geofence", "onGeofenceEnteredEvent: entered")
                    Log.d("clevertap_geofence", jsonObject.toString())
                    println("geofence entered called")
                }

                override fun onGeofenceExitedEvent(jsonObject: JSONObject) {
                    //Callback on the main thread when user exits Geofence with info in jsonObject
                    Log.d("clevertap_geofence", "onGeofenceExitedEvent: entered")
                    Log.d("clevertap_geofence", jsonObject.toString())
                    println("geofence exited called")
                }
            })

        CTGeofenceAPI.getInstance(applicationContext).setCtLocationUpdatesListener {
            //New location on the main thread as provided by the Android OS
        }

        //for deactivation
//        CTGeofenceAPI.getInstance(applicationContext).deactivate()
    }

    override fun inboxDidInitialize() {
        binding.appInbox.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's App Inbox Event")
            Toast.makeText(applicationContext, "App Inbox button Clicked", Toast.LENGTH_SHORT).show()
            cleverTapDefaultInstance?.showAppInbox()
        }
    }

    override fun inboxMessagesDidUpdate() {}

    private fun addEvents() {
        val prodViewedAction = mapOf(
            "Product Name" to "Casio Chronograph Watch",
            "Category" to "Mens Accessories",
            "Price" to 59.99,
            "Date" to Date()
        )
        cleverTapDefaultInstance?.pushEvent("Product viewed", prodViewedAction)
        Toast.makeText(applicationContext, "Event Clicked!", Toast.LENGTH_SHORT).show()
    }

    private fun addUserDetails() {
        val location = cleverTapDefaultInstance!!.location
        cleverTapDefaultInstance!!.location = location
        Log.d("location", "Latitude: ${location.latitude} longitude: ${location.longitude}")

        val profileUpdate = HashMap<String, Any>()
        profileUpdate["Name"] = "Daenerys Targaryen"
        profileUpdate["Identity"] = 6212121
        profileUpdate["Email"] = "daenerys@gmail.com"
        profileUpdate["Phone"] = "+14155551234"
        profileUpdate["Gender"] = "F"
        profileUpdate["Tz"] = "Asia/Kolkata"
        profileUpdate["Photo"] =
            "https://i.pinimg.com/564x/e2/b8/31/e2b831580c613ceda63d3dfa04caf27c.jpg"
        profileUpdate["DOB"] = SimpleDateFormat("MMM dd, yyyy").parse("Jan 21, 1991")
        profileUpdate["Employed"] = "Y"
        profileUpdate["Education"] = "Graduate"
        profileUpdate["Married"] = "N"
        profileUpdate["Customer Type"] = "Silver"
        profileUpdate["latitude"] = location.latitude
        profileUpdate["longitude"] = location.longitude

        profileUpdate["MSG-email"] = true // Disable email notifications
        profileUpdate["MSG-push"] = true // Enable push notifications
        profileUpdate["MSG-sms"] = true // Disable SMS notifications
//        profileUpdate["MSG-dndPhone"] = true // Opt out phone
//        profileUpdate["MSG-dndEmail"] = true // Opt out email
        profileUpdate["MSG-whatsapp"] = true

        profileUpdate["MyStuff"] = arrayListOf("bag", "shoes") //ArrayList of Strings
        profileUpdate["MyStuff"] = arrayOf("Jeans", "Perfume") //String Array
//        cleverTapDefaultInstance?.pushProfile(profileUpdate)
        CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profileUpdate)
    }

    private fun addUserDetails1() {
        val location = cleverTapDefaultInstance!!.location
        cleverTapDefaultInstance!!.location = location

        val profileUpdate = HashMap<String, Any>()
        profileUpdate["Name"] = "Thomas Shelby"
        profileUpdate["Identity"] = 62121223
        profileUpdate["Email"] = "thomas.shelby@gmail.com"
        profileUpdate["Phone"] = "+14155001234"
        profileUpdate["Gender"] = "M"
        profileUpdate["Tz"] = "Asia/Kolkata"
        profileUpdate["Photo"] =
            "https://i.redd.it/r1yapotvwv161.jpg"
        profileUpdate["DOB"] = SimpleDateFormat("MMM dd, yyyy").parse("Jun 11, 1994")
        profileUpdate["Employed"] = "Y"
        profileUpdate["Education"] = "Graduate"
        profileUpdate["Married"] = "N"
        profileUpdate["Customer Type"] = "Platinum"
        profileUpdate["latitude"] = location.latitude
        profileUpdate["longitude"] = location.longitude

        profileUpdate["MSG-email"] = true // Disable email notifications
        profileUpdate["MSG-push"] = true // Enable push notifications
        profileUpdate["MSG-sms"] = true // Disable SMS notifications
//        profileUpdate["MSG-dndPhone"] = true // Opt out phone
//        profileUpdate["MSG-dndEmail"] = true // Opt out email
        profileUpdate["MSG-whatsapp"] = true

        profileUpdate["MyStuff"] = arrayListOf("bag", "shoes") //ArrayList of Strings
        profileUpdate["MyStuff"] = arrayOf("Jeans", "Perfume") //String Array
//        cleverTapDefaultInstance?.pushProfile(profileUpdate)
        CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profileUpdate)
//        Toast.makeText(applicationContext, "Profile Updated!", Toast.LENGTH_SHORT).show()
    }

}

