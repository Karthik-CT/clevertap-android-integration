package com.project.integrationsdk.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.variables.Var
import com.project.integrationsdk.R
import com.project.integrationsdk.adapter.GenericAdapter
import com.project.integrationsdk.databinding.ActivityProductExperiencesNewBinding
import com.project.integrationsdk.model.RecyclerViewItem
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class ProductExperiencesNewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductExperiencesNewBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    private lateinit var testVars: List<Var<String>>
    private lateinit var theme: Var<String>
    private val TAG = "ProductExpNewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductExperiencesNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        callProductExperienceNew()

        binding.viewAll.setOnClickListener {
            restartApp(this@ProductExperiencesNewActivity)
        }
    }

    private fun callProductExperienceNew() {
        binding.skeletonUi.visibility = View.VISIBLE
        binding.mainUi.visibility = View.GONE

        theme = cleverTapDefaultInstance!!.defineVariable("theme", "loyalty")
        val varNames = listOf("test_var_string", "test_var_string2", "test_var_string3", "test_var_string4", "test_var_string5", "test_var_string6", "offer_banner", "offer_new_arrivals", "offer_categories", "kfc_banner", "kfc_categories", "kfc_new_arrivals", "kfc_banner_update")
        testVars = varNames.mapIndexed { index, name ->
            cleverTapDefaultInstance!!.defineVariable(name, "This is product experiences new testing$index")
        }

        cleverTapDefaultInstance!!.syncVariables()

        cleverTapDefaultInstance!!.fetchVariables {
            val values = varNames.map { name ->
                cleverTapDefaultInstance!!.getVariableValue(name).toString()
            }
            val themeValueFetched = cleverTapDefaultInstance!!.getVariableValue("theme").toString()
            val offer_categories_value_fetched = cleverTapDefaultInstance!!.getVariableValue("offer_categories").toString()
            val offer_banner_value_fetched = cleverTapDefaultInstance!!.getVariableValue("offer_banner").toString()
            val offer_new_arrivals_value_fetched = cleverTapDefaultInstance!!.getVariableValue("offer_new_arrivals").toString()
            Log.d(TAG, "PE Values: $values")
            Log.d(TAG, "ThemeValues: $themeValueFetched")
            Log.d(TAG, "offer_categories_value_fetched: $offer_categories_value_fetched offer_banner_value_fetched: $offer_banner_value_fetched offer_new_arrivals_value_fetched: $offer_new_arrivals_value_fetched")

            val rakeshTest = cleverTapDefaultInstance!!.getVariableValue("rakeshTest").toString()
            Log.d(TAG, "RakeshTest: $rakeshTest")
            runOnUiThread {
                renderContent(values, themeValueFetched)
                binding.skeletonUi.visibility = View.GONE
                binding.mainUi.visibility = View.VISIBLE
                renderBGColor(rakeshTest)
            }
        }
    }

    private fun renderBGColor(varTest: String) {
        when (varTest.lowercase(Locale.ROOT)) {
            "Blue".lowercase(Locale.ROOT) -> {
                binding.mainRelLay.setBackgroundColor(Color.BLUE)
            }
            "Red".lowercase(Locale.ROOT) -> {
                binding.mainRelLay.setBackgroundColor(Color.RED)
            }
            "White".lowercase(Locale.ROOT) -> {
                binding.mainRelLay.setBackgroundColor(Color.WHITE)
            }
        }
    }

    private fun renderContent(values: List<String>, themeValue: String) {
        when (themeValue.lowercase(Locale.ROOT)) {
            "loyalty".lowercase(Locale.ROOT) -> {
                Toast.makeText(applicationContext, themeValue, Toast.LENGTH_SHORT).show()
                renderCarousel(values[0])
                renderRestaurant(values[1])
                renderMerchant(values[2])
            }
            "e-commerce".lowercase(Locale.ROOT) -> {
                Toast.makeText(applicationContext, themeValue, Toast.LENGTH_SHORT).show()
    //            renderCarousel(values[3])
    //            renderRestaurant(values[4])
                renderCarousel(values[0])
                renderRestaurant(values[1])
                renderMerchant(values[5])
            }
            "ramadan".lowercase(Locale.ROOT) -> {
                Toast.makeText(applicationContext, themeValue, Toast.LENGTH_SHORT).show()
                binding.sectionTitle2.text = "New Arrivals"
                binding.sectionTitle3.text = "Popular Categories"
                renderCarousel(values[6])
                renderNewArrivals(values[7])
                renderMerchant(values[8])
            }
            "kfc".lowercase(Locale.ROOT) -> {
                Toast.makeText(applicationContext, themeValue, Toast.LENGTH_SHORT).show()
                binding.sectionTitle2.text = "Top Deals of the day \uD83D\uDD25"
                binding.sectionTitle3.text = "Explore Menu"
                renderCarousel(values[9])
                renderMerchant(values[10])
                renderNewArrivals(values[11])
            }
            "kfc_banner_update".lowercase(Locale.ROOT) -> {
                Toast.makeText(applicationContext, themeValue, Toast.LENGTH_SHORT).show()
                binding.sectionTitle2.text = "Top Deals of the day \uD83D\uDD25"
                binding.sectionTitle3.text = "Explore Menu"
                renderCarousel(values[12])
                renderMerchant(values[10])
                renderNewArrivals(values[11])
            }
        }
    }

    private fun switchAppIcon(isFestival: Boolean) {
        val pm = packageManager

        val defaultAlias = "com.project.integrationsdk.ECommerceLauncher"
        val festivalAlias = "com.project.integrationsdk.FestivalLauncher"

        // Always disable both first
        pm.setComponentEnabledSetting(
            ComponentName(this, defaultAlias),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            ComponentName(this, festivalAlias),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        // Now enable only the required alias
        val aliasToEnable = if (isFestival) festivalAlias else defaultAlias
        pm.setComponentEnabledSetting(
            ComponentName(this, aliasToEnable),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun renderCarousel(value: String) {
        val autoScrollHandler = Handler(Looper.getMainLooper())
        lateinit var autoScrollRunnable: Runnable
        autoScrollRunnable = Runnable {
            binding.carouselViewPager.let {
                it.currentItem = (it.currentItem + 1) % (it.adapter?.itemCount ?: 1)
                autoScrollHandler.postDelayed(autoScrollRunnable, 5000)
            }
        }

        fun autoScroll(start: Boolean) = with(autoScrollHandler) {
            removeCallbacks(autoScrollRunnable)
            if (start) postDelayed(autoScrollRunnable, 5000)
        }

        renderItems(value,
            { index -> listOf("image_url_$index", "title$index", "subtitle$index") },
            { values -> RecyclerViewItem.Carousel(values[0], values[1], values[2]) }
        ) { items ->
            val capsules = MutableList(items.size) { RecyclerViewItem.Capsule(isSelected = it == 0) }
            val genericAdapter = GenericAdapter(capsules) {
                binding.carouselViewPager.currentItem = capsules.indexOf(it)
                autoScroll(false).also { autoScroll(true) }
            }

            binding.capsuleRecyclerView.adapter = genericAdapter
            binding.capsuleRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.carouselViewPager.adapter = GenericAdapter(items) { item ->
                Toast.makeText(applicationContext, "$item", Toast.LENGTH_SHORT).show()
            }

            autoScroll(true)

            binding.carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    capsules.forEachIndexed { index, _ -> capsules[index] = RecyclerViewItem.Capsule(isSelected = index == position) }
                    genericAdapter.run { notifyDataSetChanged() }
                    autoScroll(false).also { autoScroll(true) }
                }
            })
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) = autoScroll(false)
        })
    }

    private fun renderMerchant(value: String) {
        renderItems(
            value,
            keysProvider = { index -> listOf("logo_image_url_$index", "logo_name_$index") },
            itemBuilder = { values -> RecyclerViewItem.Merchant(values[0], values[1]) },
            setupRecyclerView = { items -> setupRecyclerView(binding.merchantsRecyclerView, items) }
        )
    }

    private fun renderNewArrivals(value: String) {
        renderItems(
            value,
            keysProvider = { index -> listOf("new_arrival_image_url_$index", "new_arrival_name_$index", "new_arrival_rating_$index", "new_arrival_price_$index", "new_arrival_offer_$index") },
            itemBuilder = { values -> RecyclerViewItem.Restaurant(values[0], values[1], values[2], values[3], values[4]) },
            setupRecyclerView = { items -> setupRecyclerView(binding.restaurantRecyclerView, items) }
        )
    }

    private fun renderRestaurant(value: String) {
        renderItems(
            value,
            keysProvider = { index -> listOf("restaurant_image_url_$index", "restaurant_name_$index", "restaurant_rating_$index", "restaurant_distance_$index", "restaurant_offer_$index") },
            itemBuilder = { values -> RecyclerViewItem.Restaurant(values[0], values[1], values[2], values[3], values[4]) },
            setupRecyclerView = { items -> setupRecyclerView(binding.restaurantRecyclerView, items) }
        )
    }

    private fun <T> renderItems(value: String, keysProvider: (index: Int) -> List<String>, itemBuilder: (values: List<String>) -> T, setupRecyclerView: (List<T>) -> Unit) {
        val items = mutableListOf<T>()
        val peValues = try {
            JSONObject(value)
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON: $e")
            return
        }

        var i = 1
        while (true) {
            val keys = keysProvider(i)
            if (keys.all { peValues.has(it) }) {
                val values = keys.map { peValues.getString(it) }
                items.add(itemBuilder(values))
            } else break
            i++
        }

        setupRecyclerView(items)
    }

    private fun <T : RecyclerViewItem> setupRecyclerView(recyclerView: RecyclerView, items: List<T>) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ProductExperiencesNewActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = GenericAdapter(items) { item ->
                when (item) {
                    is RecyclerViewItem.Merchant -> Toast.makeText(context, "Clicked on Merchant: ${item.name}", Toast.LENGTH_SHORT).show()
                    is RecyclerViewItem.Restaurant -> Toast.makeText(context, "Clicked on Restaurant: ${item.name}", Toast.LENGTH_SHORT).show()
                    is RecyclerViewItem.Carousel -> Toast.makeText(context, "Clicked on Carousel", Toast.LENGTH_SHORT).show()
                    is RecyclerViewItem.Capsule -> Toast.makeText(context, "Clicked on Capsule", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun restartApp(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                restartApp(this) // Call your refresh logic
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
