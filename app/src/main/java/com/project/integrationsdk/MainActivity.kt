package com.project.integrationsdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.clevertap.android.geofence.CTGeofenceAPI
import com.clevertap.android.geofence.CTGeofenceSettings
import com.clevertap.android.geofence.Logger
import com.clevertap.android.geofence.interfaces.CTGeofenceEventsListener
import com.clevertap.android.geofence.interfaces.CTLocationUpdatesListener
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler
import com.clevertap.android.sdk.*
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.clevertap.android.sdk.interfaces.NotificationHandler
import com.clevertap.android.sdk.product_config.CTProductConfigListener
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.facebook.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.project.integrationsdk.databinding.ActivityMainBinding
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Properties.Product
import com.segment.analytics.Traits
import com.segment.analytics.android.integrations.clevertap.CleverTapIntegration
//import com.singular.sdk.Singular
//import com.singular.sdk.SingularConfig
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), InAppNotificationButtonListener,
    DisplayUnitListener, CTInboxListener, InboxMessageButtonListener, InboxMessageListener, CTPushNotificationListener, CTGeofenceAPI.OnGeofenceApiInitializedListener,
    CTGeofenceEventsListener, CTLocationUpdatesListener {

    lateinit var binding: ActivityMainBinding
    var cleverTapDefaultInstance: CleverTapAPI? = null
    var ctGeofenceAPI: CTGeofenceAPI? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkLocationPermission()

//        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_baseline_notifications_24)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)
        ctGeofenceAPI = CTGeofenceAPI.getInstance(applicationContext)

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

        cleverTapDefaultInstance?.ctPushNotificationListener = this

        //push templates
        CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler() as NotificationHandler)

        cleverTapDefaultInstance?.enableDeviceNetworkInfoReporting(true)

        //addUserDetails()
0
        binding.updateProfile.setOnClickListener {
            var prof = HashMap<String, Any>()
            prof["Email"] = "test110@test.com"
            prof["Identity"] = "test110"
            cleverTapDefaultInstance?.pushProfile(prof)
            Toast.makeText(applicationContext, "Profile Updated!", Toast.LENGTH_SHORT).show()
        }
        binding.addEvents.setOnClickListener {
            addEvents()
            Toast.makeText(applicationContext, "Events button Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.pushNotification.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Noti Event")
            Toast.makeText(applicationContext, "Notification button Clicked", Toast.LENGTH_SHORT)
                .show()
        }
        binding.pushNotification2.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("KarthikNotiEventNew")
            Toast.makeText(applicationContext, "PN button Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.inapp.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's InApp Event")
            Toast.makeText(applicationContext, "InApp button Clicked", Toast.LENGTH_SHORT).show()
        }
        cleverTapDefaultInstance?.apply {
            ctNotificationInboxListener = this@MainActivity
            initializeInbox()
            val all_msgs = allInboxMessages

            all_msgs.forEach { ctInboxMessage ->
                val msg = ctInboxMessage.inboxMessageContents
                println("KK ALL AI Messages: $msg")
                msg.forEach { ctInboxMessageContent ->
                    println("Payload=====> Title: ${ctInboxMessageContent.title} Message: ${ctInboxMessageContent.message} Links: ${ctInboxMessageContent.links}")
                }
            }
        }
        binding.nativeDisplay.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Native Display Event")
            startActivity(Intent(applicationContext, NativeDisplayActivity::class.java))
            Toast.makeText(applicationContext, "Native Display button Clicked", Toast.LENGTH_SHORT)
                .show()
        }
        binding.geofence.setOnClickListener {
            startActivity(Intent(applicationContext, GeofenceActivity::class.java))
            Toast.makeText(applicationContext, "Geofence button Clicked", Toast.LENGTH_SHORT).show()
        }

        //GeoFence
//        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(applicationContext, "Please grant the Permission", Toast.LENGTH_SHORT).show()
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
//        }
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(applicationContext, "Please grant the Permission", Toast.LENGTH_SHORT).show()
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1)
//        }
//        else {
//            Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
////            geoFencing()
//        }

        geoFencing()

        //custom app inbox
        binding.customAppInbox.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's App Inbox Event")
            Toast.makeText(
                applicationContext,
                "Custom App Inbox button Clicked",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(applicationContext, CustomAppInboxActivity::class.java))
        }

        //product experience
        binding.productExp.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Product Exp Event")
//            productExperienceAB()
            startActivity(Intent(applicationContext, ProductExperienceActivity::class.java))
            Toast.makeText(applicationContext, "Product Exp button Clicked", Toast.LENGTH_SHORT)
                .show()
        }

        //in app callbacks
        cleverTapDefaultInstance!!.setInAppNotificationButtonListener(this)
        binding.inappCallback.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's InApp Callback Event")
            Toast.makeText(applicationContext, "InApp Callback Event Clicked", Toast.LENGTH_SHORT)
                .show()
        }

        //web view
        binding.webView.setOnClickListener {
            startActivity(Intent(applicationContext, WebViewActivity::class.java))
            Toast.makeText(applicationContext, "WebView Button Clicked", Toast.LENGTH_SHORT).show()
        }

        //push template button
        binding.pushTemplate.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Push Template Event")
            Toast.makeText(applicationContext, "Push Template Event Clicked", Toast.LENGTH_SHORT)
                .show()
        }

        //Custom Android Push Notifications Handling
//        FirebaseMessaging.getInstance().token.addOnSuccessListener {res->
//            if(res != null) {
//                println("res= $res")
//                cleverTapDefaultInstance?.pushFcmRegistrationId(res, true)
//            }
//        }

        //Segment - Android
        binding.segmentsAndroid.setOnClickListener {
            val analytics = Analytics.Builder(
                this,
                "Yhppg7GRbCW2jQ27Pu4EzvIxDTMqH2J0"
            )//add your write key from segments
                .logLevel(Analytics.LogLevel.DEBUG)
                .use(CleverTapIntegration.FACTORY)
                .build()

            //identify
            val traits = Traits()
            traits.putEmail("karthik@gmail.com")
            traits["string"] = "hello"
            analytics.identify("kk1234", traits, null)

            //track
            val orderId = "981611"
            val revenue = 100
            val properties = Properties()
            properties.putValue("orderId", orderId).putValue("revenue", revenue)

            val product1 = Product("id1", "sku1", 100.0)
            val product2 = Product("id2", "sku2", 200.0)
            properties.putProducts(product1, product2)
            analytics.track("Segment Order Completed", properties)
        }

        //Bulletins
        binding.bulletins.setOnClickListener {
            val bulletinsAction = mapOf(
                "Episode Season" to "20",
                "Episode Number" to "1014",
            )
            cleverTapDefaultInstance?.pushEvent("KKBulletins Pressed", bulletinsAction)
            Toast.makeText(applicationContext, "Bulletins Event Clicked!", Toast.LENGTH_SHORT)
                .show()
        }

        //Image Only Native Display
        cleverTapDefaultInstance?.setDisplayUnitListener(this)
        binding.imageNd.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Image ND Event")
            Toast.makeText(
                applicationContext,
                "Image Native Display button Clicked",
                Toast.LENGTH_SHORT
            ).show()
        }

        //Catalogs
        binding.catalog.setOnClickListener {
            val cat = mapOf(
                "Identity" to "CT000001",
                "Name" to "CleverTap Orange Hoodie",
                "ImageURL" to "https://5.imimg.com/data5/JO/XM/MY-49561403/hoodie2-500x500.jpg",
                "total_pdp_amt" to 700,
                "pdp_view" to 950,
                "pdp_name" to "Red Hoodie",
                "pdp_image_url" to "https://5.imimg.com/data5/US/GA/MY-21588216/men-hoodies-500x500.jpg"
            )
            cleverTapDefaultInstance?.pushEvent("Karthik's Catalog", cat)
            Toast.makeText(applicationContext, "Catalogs button Clicked", Toast.LENGTH_SHORT).show()
        }

        //MixPanel Integration
        binding.mixpanel.setOnClickListener {
            val mixpanel = MixpanelAPI.getInstance(this, "88a0ce6fad1ea42493f2d4e87fd234a7")
//        val mixpanel = MixpanelAPI.getInstance(this, "41c0d5f6c70a47e47964a2cbee2e7396")
            val props = JSONObject()
            props.put("source", "Pat's affiliate site")
            props.put("Opted out of email", true)

            //MP Exports
//        mixpanel.identify("kk1611")
            mixpanel.track("Sign Up", props)
            //MP Imports
//        mixpanel.people.set("CleverTap_user_id", "kk1611")
        }

        //Real Time Uninstall Tracking
        binding.rtut.setOnClickListener {
            firebaseAnalytics = FirebaseAnalytics.getInstance(this)
            val parameters = Bundle().apply {
                this.putString("level_name", "Caverns01")
                this.putInt("level_difficulty", 4)
            }
            firebaseAnalytics.setDefaultEventParameters(parameters)
            firebaseAnalytics.setUserProperty(
                "ct_objectId",
                Objects.requireNonNull(CleverTapAPI.getDefaultInstance(this))!!.cleverTapID
            )
        }

        //Feature Flags
        binding.featureFlags.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Feature Flag Event")
            productExperienceFeatureFlag()
            Toast.makeText(applicationContext, "Feature Flag button Clicked", Toast.LENGTH_SHORT)
                .show()
        }

        //Emails
        binding.emailBtn.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Email Event")
            Toast.makeText(applicationContext, "Email button Clicked", Toast.LENGTH_SHORT).show()
        }

        //AppsFlyer
        AppsFlyerLib.getInstance().init("ARphSC736QeqLtvqA5TmHX", null, this)
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance().start(this, "ARphSC736QeqLtvqA5TmHX",
            object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.d("af", "Launch sent successfully")
                }

                override fun onError(errorCode: Int, errorDesc: String) {
                    Log.d(
                        "af", "Launch failed to be sent:\n" +
                                "Error code: " + errorCode + "\n"
                                + "Error description: " + errorDesc
                    )
                }
            })

        cleverTapDefaultInstance?.getCleverTapID {
            AppsFlyerLib.getInstance().setCustomerUserId(it)
        }
        AppsFlyerLib.getInstance().setDebugLog(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            afPE()
        }

        //Product Config
        binding.productConfigBtn.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's Product Config Event")
            productConfig()
            Toast.makeText(applicationContext, "Product Config Clicked", Toast.LENGTH_SHORT).show()
        }

        //mParticle
        binding.mparticle.setOnClickListener {

        }

        binding.addToCart.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("add to cart")
            Toast.makeText(applicationContext, "Add to cart Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.chargedBtn.setOnClickListener {
            val chargeDetails = HashMap<String, Any>()
            chargeDetails["Amount"] = 300
            chargeDetails["Payment Mode"] = "Credit card"
            chargeDetails["Charged ID"] = 24052013

            val item1 = HashMap<String, Any>()
            item1["Product category"] = "books"
            item1["Book name"] = "The Millionaire next door"
            item1["Quantity"] = 1

            val items = ArrayList<HashMap<String, Any>>()
            items.add(item1)

            cleverTapDefaultInstance?.pushChargedEvent(chargeDetails, items)
            Toast.makeText(applicationContext, "Charged Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.chargedBtnStr.setOnClickListener {
            val chargeDetails = HashMap<String, Any>()
            chargeDetails["Amount"] = "200"
            chargeDetails["Payment Mode"] = "Credit card"
            chargeDetails["Charged ID"] = 24052033

            val item1 = HashMap<String, Any>()
            item1["Product category"] = "books"
            item1["Book name"] = "The Billionaire next door"
            item1["Quantity"] = 1

            val items = ArrayList<HashMap<String, Any>>()
            items.add(item1)

            cleverTapDefaultInstance?.pushChargedEvent(chargeDetails, items)
            Toast.makeText(applicationContext, "Charged Clicked", Toast.LENGTH_SHORT).show()
        }


        binding.chargedBtnFloat.setOnClickListener {
            val chargeDetails = HashMap<String, Any>()
            chargeDetails["Amount"] = 200.50
            chargeDetails["Payment Mode"] = "Credit card"
            chargeDetails["Charged ID"] = 24052033

            val item1 = HashMap<String, Any>()
            item1["Product category"] = "books"
            item1["Book name"] = "The Trillionaire next door"
            item1["Quantity"] = 1

            val items = ArrayList<HashMap<String, Any>>()
            items.add(item1)

            cleverTapDefaultInstance?.pushChargedEvent(chargeDetails, items)
            Toast.makeText(applicationContext, "Charged Clicked", Toast.LENGTH_SHORT).show()
        }

        //Singular Integration
//        val singularConfig = SingularConfig(
//            "0c2efa8657e934b3728e69f257827b74e46c0006e7f9a41383b3a938aab1ccfe",
//            "7a63ca5dda084505221f75ca1b415e37"
//        )
//            .withCustomUserId("kkiyer16")
//            .withSessionTimeoutInSec(120)

//        Singular.init(applicationContext, singularConfig)
//
//        cleverTapDefaultInstance?.getCleverTapID {
//            Singular.setGlobalProperty("CLEVERTAPID", it, true)
//        }
//        Singular.event("KKSingularTest")

        //App Personalization
        //Enable Personalization
        cleverTapDefaultInstance?.enablePersonalization()
        //Accessing User Profile Data
        cleverTapDefaultInstance?.getProperty("Customer Type")
        println("customer type: ${cleverTapDefaultInstance?.getProperty("Customer Type")}")
        val myStuff = cleverTapDefaultInstance?.getProperty("MyStuff")
        println("Stuff: $myStuff")

        FacebookSdk.sdkInitialize(applicationContext)
        val params = Bundle()
        params.putString("name", "My new Custom Audience")
        params.putString("subtype", "CUSTOM")
        params.putString("description", "People who purchased on my website")
        params.putString("customer_file_source", "USER_PROVIDED_ONLY")
        GraphRequest(
            AccessToken.getCurrentAccessToken(),
            "/act_514641083667285/customaudiences",
            params,
            HttpMethod.POST,
            object : GraphRequest.Callback {
                override fun onCompleted(response: GraphResponse) {
                    /* handle the result */
                    println("Response: $response")
                }
            }
        ).executeAsync()

        //uploadEvents
        binding.uploadEventsBtn.setOnClickListener {
            binding.eventUploadRellay.visibility = View.VISIBLE
        }

        binding.uploadEvents.setOnClickListener {
            val eventProp = mapOf(
                binding.eventPropKey.text.toString() to binding.eventPropValue.text.toString()
            )
            cleverTapDefaultInstance?.pushEvent(binding.eventToUpload.text.toString(), eventProp)
            Toast.makeText(applicationContext, "Event Pushed", Toast.LENGTH_SHORT).show()
        }

        binding.OptInBtn.setOnClickListener {
            cleverTapDefaultInstance!!.setOptOut(false)
            Toast.makeText(applicationContext, "Opted In", Toast.LENGTH_SHORT).show()
        }

        binding.OptOutBtn.setOnClickListener {
            cleverTapDefaultInstance!!.setOptOut(true)
            Toast.makeText(applicationContext, "Opted Out", Toast.LENGTH_SHORT).show()

        }

        //Logout Functionality
        binding.logoutBtn.setOnClickListener {
            logOutSession()
            Toast.makeText(applicationContext, "Logged out!", Toast.LENGTH_SHORT).show()

        }

        //loan button
        binding.loanEvent.setOnClickListener {
            val loanBtnHashmap = HashMap<String, Any>()
            loanBtnHashmap["loan_amount"] = 4500000.00
            loanBtnHashmap["loan_disbursed_date"] =
                SimpleDateFormat("MMM dd, yyyy").parse("Aug 28, 2022")
            loanBtnHashmap["loan_type"] = "First Loan"
            loanBtnHashmap["loan_duration"] = "365 days"
            cleverTapDefaultInstance?.pushEvent("Loan Disbursed", loanBtnHashmap)
            Toast.makeText(applicationContext, "Loan Disbursed Event Clicked!", Toast.LENGTH_SHORT)
                .show()
        }

        //open website
        binding.openWebsite.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://web-integration-sdk.000webhostapp.com/"))
            startActivity(i)
        }

        //set offline true
        binding.offlineTrueBtn.setOnClickListener {
            cleverTapDefaultInstance?.setOffline(true)
            Toast.makeText(applicationContext, "setOffline(true)", Toast.LENGTH_SHORT).show()
        }

        //set offline false
        binding.offlineFalseBtn.setOnClickListener {
            cleverTapDefaultInstance?.setOffline(false)
            Toast.makeText(applicationContext, "setOffline(false)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("resume", "displaying inapp from onResume")
        //resume inapp
        cleverTapDefaultInstance?.resumeInAppNotifications()
//        geoFencing()

    }

    fun logOutSession() {
        val sharedPreferences = getSharedPreferences("WizRocket", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        CleverTapAPI.getInstances().clear()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun afPE() {
        val productConfigInstance = cleverTapDefaultInstance!!.productConfig()
        val hashMap = HashMap<String, Any>()
        hashMap["AFKey"] = "AF Key PE Test" //add these key value pair in dashboard keys
        productConfigInstance.setDefaults(hashMap)

        //FetchAndActivate +20 114 1332514
        productConfigInstance.fetchAndActivate()
        Log.d("pe", productConfigInstance.fetchAndActivate().toString())

        //register listener
        cleverTapDefaultInstance!!.setCTProductConfigListener(object : CTProductConfigListener {
            override fun onActivated() {
                val pe_act = cleverTapDefaultInstance!!.productConfig().getString("AFKey")
                Log.d("pe_act", pe_act)
                when (pe_act) {
                    "AF Key PE Test" -> {
                        binding.appsflyerPe.text = pe_act
                        binding.appsflyerPe.setTextColor(Color.GREEN)
                    }
                    "AF Varaint A" -> {
                        binding.appsflyerPe.text = pe_act
                        binding.appsflyerPe.setTextColor(Color.BLUE)
                    }
                    "AF Varaint B" -> {
                        binding.appsflyerPe.text = pe_act
                        binding.appsflyerPe.setTextColor(Color.CYAN)
                    }
                }
            }

            override fun onFetched() {
                val pe_fet = cleverTapDefaultInstance!!.productConfig().getString("AFKey")
                Log.d("pe_fet", pe_fet)
            }

            override fun onInit() {
                productConfigInstance.fetch()
                productConfigInstance.activate()
            }
        })

        //throttling
        productConfigInstance.setMinimumFetchIntervalInSeconds(60 * 10)

        //get parameter values
        productConfigInstance.getBoolean("key_bool")
        productConfigInstance.getDouble("key_double")
        productConfigInstance.getLong("key_long")
        productConfigInstance.getString("key_string")
        productConfigInstance.getString("key_json")

        //resetting the configs
//        productConfigInstance.reset()

        //last response time stamp
        productConfigInstance.lastFetchTimeStampInMillis
        Log.d("Product Experience", productConfigInstance.lastFetchTimeStampInMillis.toString())


        binding.dateEvent.setOnClickListener {
            dateEvent()
        }
    }

    fun productConfig() {
        val productConfigInstance = cleverTapDefaultInstance!!.productConfig()
        val hashMap = HashMap<String, Any>()
        hashMap["ProdConfigKey"] =
            "product config test 100% original" //add these key value pair in dashboard keys
        productConfigInstance.setDefaults(hashMap)

        //FetchAndActivate
        productConfigInstance.fetchAndActivate()
        Log.d("pe", productConfigInstance.fetchAndActivate().toString())

        //register listener
        cleverTapDefaultInstance!!.setCTProductConfigListener(object : CTProductConfigListener {
            override fun onActivated() {
                val pe_act = cleverTapDefaultInstance!!.productConfig().getString("ProdConfigKey")
                Log.d("pe_act", pe_act)
                when (pe_act) {
                    "product config test 100% original" -> {
                        Toast.makeText(applicationContext, "fetched", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(applicationContext, "not fetched", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFetched() {
                val pe_fet = cleverTapDefaultInstance!!.productConfig().getString("ProdConfigKey")
                Log.d("pe_fet", pe_fet)
            }

            override fun onInit() {
                productConfigInstance.fetch()
                productConfigInstance.activate()
            }
        })

        //throttling
        productConfigInstance.setMinimumFetchIntervalInSeconds(60 * 10)

        //get parameter values
        productConfigInstance.getBoolean("key_bool")
        productConfigInstance.getDouble("key_double")
        productConfigInstance.getLong("key_long")
        productConfigInstance.getString("key_string")
        productConfigInstance.getString("key_json")

        //resetting the configs
//        productConfigInstance.reset()

        //last response time stamp
        productConfigInstance.lastFetchTimeStampInMillis
        Log.d("Product Experience", productConfigInstance.lastFetchTimeStampInMillis.toString())
    }

    fun productExperienceFeatureFlag() {
        val featureFlagInstance = cleverTapDefaultInstance!!.featureFlag()
        cleverTapDefaultInstance!!.setCTFeatureFlagsListener {
        }
        when (featureFlagInstance.get("FeatFlagKey", false)) {
            true -> {
                Toast.makeText(applicationContext, "true", Toast.LENGTH_SHORT).show()
            }
            false -> {
                Toast.makeText(applicationContext, "false", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("ff", featureFlagInstance.get("FeatFlagKey", false).toString())
    }

    fun geoFencing() {
        println("KK Geo Called")
        val ctGeofenceSettings = CTGeofenceSettings.Builder()
            .enableBackgroundLocationUpdates(true)//boolean to enable background location updates
            .setLogLevel(Logger.VERBOSE)//Log Level
            .setLocationAccuracy(CTGeofenceSettings.ACCURACY_HIGH)//byte value for Location Accuracy
            .setLocationFetchMode(CTGeofenceSettings.FETCH_CURRENT_LOCATION_PERIODIC)//byte value for Fetch Mode
            .setGeofenceMonitoringCount(100)//int value for number of Geofences CleverTap can monitor
            .setInterval(10000)//long value for interval in milliseconds
            .setFastestInterval(5000)//long value for fastest interval in milliseconds
            .setSmallestDisplacement(0.3f)//float value for smallest Displacement in meters
            .setGeofenceNotificationResponsiveness(5000)// int value for geofence notification responsiveness in milliseconds
            .build()

        CTGeofenceAPI.getInstance(this).init(ctGeofenceSettings, cleverTapDefaultInstance!!)

        //callbacks
        CTGeofenceAPI.getInstance(applicationContext).setOnGeofenceApiInitializedListener {
            //App is notified on the main thread that CTGeofenceAPI is initialized
            Log.d("clevertap_geofence", "CTGeofenceAPI is initialized")
            println("CTGeofenceAPI is initialized called")
        }

        try {
            CTGeofenceAPI.getInstance(applicationContext).triggerLocation()
        } catch (e: IllegalStateException) { // thrown when this method is called before geofence SDK initialization
            e.printStackTrace()
        }

        CTGeofenceAPI.getInstance(applicationContext).setCtLocationUpdatesListener {
            //New location on the main thread as provided by the Android OS
//            Log.d("ad_geofencing", it.latitude.toString())
            if(it != null) {
                Log.d("Location updated", "" + it.latitude + " and " + it.longitude)
            }
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

        //for deactivation
//        CTGeofenceAPI.getInstance(applicationContext).deactivate()
    }

    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        println("In App payload: $payload")
    }

    override fun inboxDidInitialize() {
        binding.appInbox.setOnClickListener {
            cleverTapDefaultInstance?.pushEvent("Karthik's App Inbox Event")
            Toast.makeText(applicationContext, "App Inbox button Clicked", Toast.LENGTH_SHORT)
                .show()
            cleverTapDefaultInstance?.showAppInbox()

//            val inboxTabs = arrayListOf("Activities", "Announcements", "Others")
//            val inboxTabs = arrayListOf("Announcements")
//            CTInboxStyleConfig().apply {
//                tabs = inboxTabs
//                tabBackgroundColor = "#FF0000"
//                selectedTabIndicatorColor = "#0000FF"
//                selectedTabColor = "#000000"
//                unselectedTabColor = "#FFFFFF"
//                backButtonColor = "#FF0000"
//                navBarTitleColor = "#FF0000"
//                navBarTitle = "MY INBOX"
//                navBarColor = "#FFFFFF"
//                inboxBackgroundColor = "#00FF00"
//                firstTabTitle = "Activities"
//                cleverTapDefaultInstance?.showAppInbox(this)
//            }
        }
    }

    override fun inboxMessagesDidUpdate() {

    }

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateEvent() {
        val dateABhi = "01-01-2000".convertToDate("dd-MM-yyyy")
        val eventDate = mapOf(
            "date" to SimpleDateFormat("MMM dd, yyyy").parse("Nov 16, 1998"),
            "date2" to dateABhi
        )
        cleverTapDefaultInstance?.pushEvent("Test Date 2", eventDate)
        Toast.makeText(applicationContext, "Date Clicked", Toast.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun String.convertToDate(format: String): Date? {
        if (this.isEmpty())
            return null
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
        val date = formatter.format(LocalDate.parse(this, DateTimeFormatter.ofPattern(format)))
        val dateTime = LocalDate.parse(date, formatter).atStartOfDay(ZoneOffset.UTC).toInstant()
        return Date.from(dateTime)
    }

    private fun addUserDetails() {
        val location = cleverTapDefaultInstance!!.location
        cleverTapDefaultInstance!!.location = location
        Log.d("location", "Latitude: ${location.latitude} longitude: ${location.longitude}")

        val profileUpdate = HashMap<String, Any>()
        profileUpdate["Name"] = "Karthik Iyer"
        profileUpdate["Identity"] = "kk1611"
        profileUpdate["Email"] = "karthik.iyer@clevertap.com"
        profileUpdate["Phone"] = "+917021815311"
        profileUpdate["Gender"] = "M"
        profileUpdate["Tz"] = "Asia/Kolkata"
        val dNow = Date()
        val ft = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")
        profileUpdate["DOB"] = ft.format(dNow)
        profileUpdate["DOB"] = SimpleDateFormat("MMM dd, yyyy").parse("Nov 16, 1998")
        profileUpdate["Employed"] = "Y"
        profileUpdate["Education"] = "Graduate"
        profileUpdate["Married"] = "N"
        profileUpdate["Customer Type"] = "Gold"
        profileUpdate["latitude"] = location.latitude
        profileUpdate["longitude"] = location.longitude

        profileUpdate["MSG-email"] = true // Disable email notifications
        profileUpdate["MSG-push"] = true // Enable push notifications
        profileUpdate["MSG-sms"] = true // Disable SMS notifications
        profileUpdate["MSG-whatsapp"] = true

        profileUpdate["MyStuff"] = arrayListOf("bag", "shoes") //ArrayList of Strings
        profileUpdate["MyStuff"] = arrayOf("Jeans", "Perfume") //String Array
        CleverTapAPI.getDefaultInstance(applicationContext)?.onUserLogin(profileUpdate)
    }

    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        println("Image Notification ND: $units")
        for (i in 0 until units!!.size) {
            prepareDisplayUnit(units[i])
        }
    }

    private fun prepareDisplayUnit(unit: CleverTapDisplayUnit) {
        unit.contents.forEach {
            println(it.media)
        }
        CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitViewedEventForID(unit.unitID)
        CleverTapAPI.getDefaultInstance(this)?.pushDisplayUnitClickedEventForID(unit.unitID)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            CleverTapAPI.getDefaultInstance(this)?.pushNotificationClickedEvent(intent!!.extras)
        }

        val payload = this.intent?.extras
        if (payload?.containsKey("pt_id") == true && payload["pt_id"] =="pt_rating")
        {
            val nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(payload["notificationId"] as Int)
        }
        if (payload?.containsKey("pt_id") == true && payload["pt_id"] =="pt_product_display")
        {
            val nm = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(payload["notificationId"] as Int)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onInboxButtonClick(payload: HashMap<String, String>?) {
        Log.d("onInboxButtonClick", "Inbox button clicked")
    }

//    override fun onInboxItemClicked(message: CTInboxMessage?) {
//        println("clicked app inbox")
//        println("AI Payload Message: $message")
//    }

    override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>?) {
        println("PUSH KK PAYLOAD: $payload")
    }

    override fun onInboxItemClicked(
        message: CTInboxMessage?,
        contentPageIndex: Int,
        buttonIndex: Int
    ) {
        TODO("Not yet implemented")
    }

    fun onInboxItemClicked(message: CTInboxMessage?) {
        TODO("Not yet implemented")
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        requestLocationPermission()
                    }
                    .create()
                    .show()
            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission()
            }
        } else {
            checkBackgroundLocation()
        }

    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_LOCATION
        )
    }

    private fun checkBackgroundLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        checkBackgroundLocation()
                    }

                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()

                }
                return
            }
            MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Toast.makeText(
                            this,
                            "Granted Background Location Permission",
                            Toast.LENGTH_LONG
                        ).show()

                        geoFencing()
                    }
                } else {

                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return

            }
        }
    }

    override fun OnGeofenceApiInitialized() {
        try {
            Log.d(
                "clevertap OnGeofenceApiInitialized-",
                "-----OnGeofenceApiInitialized----="
            )
        } catch (e: Exception) {

        }
    }

    override fun onGeofenceEnteredEvent(geofenceEnteredEventProperties: JSONObject?) {
        try {
            Log.d(
                "Geofence Entered",
                geofenceEnteredEventProperties!!.getString("gcName") + " : " + geofenceEnteredEventProperties!!.getString(
                    "triggered_lat"
                ) + " , " + geofenceEnteredEventProperties!!.getString("triggered_lng")
            )
        } catch (e: Exception) {
            Log.d("Exception : ", e.localizedMessage)
        }
    }

    override fun onGeofenceExitedEvent(geofenceExitedEventProperties: JSONObject?) {
        try {
            Log.d(
                "Geofence Entered",
                geofenceExitedEventProperties!!.getString("gcName") + " : " + geofenceExitedEventProperties!!.getString(
                    "triggered_lat"
                ) + " , " + geofenceExitedEventProperties!!.getString("triggered_lng")
            )
        } catch (e: Exception) {
            Log.d("Exception : ", e.localizedMessage)
        }
    }

    override fun onLocationUpdates(location: Location?) {
        try {
            Log.d(
                "Location Updated : ",
                location!!.latitude.toString() + " , " + location!!.longitude.toString()
            )

        } catch (e: Exception) {
            Log.d("Exception : ", e.localizedMessage)
        }
    }

}