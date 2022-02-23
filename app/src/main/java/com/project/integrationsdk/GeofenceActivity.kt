package com.project.integrationsdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.clevertap.android.geofence.CTGeofenceAPI
import com.clevertap.android.geofence.CTGeofenceAPI.OnGeofenceApiInitializedListener
import com.clevertap.android.geofence.CTGeofenceSettings
import com.clevertap.android.geofence.Logger
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener
import com.clevertap.android.geofence.interfaces.CTLocationUpdatesListener
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.databinding.ActivityGeofenceBinding
import org.json.JSONObject
import java.util.jar.Attributes
import kotlin.math.log

class GeofenceActivity : AppCompatActivity() {

    lateinit var binding: ActivityGeofenceBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null
    var ctGeofenceAPI : CTGeofenceAPI? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeofenceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        ctGeofenceAPI = CTGeofenceAPI.getInstance(applicationContext)

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "Please grant the Permission", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            geoFencing()
        }
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
                    Log.d("kk", "onGeofenceEnteredEvent: entered")
                    Log.d("kk", jsonObject.toString())
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

}

