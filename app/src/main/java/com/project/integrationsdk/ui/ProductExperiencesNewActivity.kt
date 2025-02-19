package com.project.integrationsdk.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.variables.Var
import com.project.integrationsdk.adapter.GenericAdapter
import com.project.integrationsdk.databinding.ActivityProductExperiencesNewBinding
import com.project.integrationsdk.model.RecyclerViewItem
import org.json.JSONException
import org.json.JSONObject

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
    }

    private fun callProductExperienceNew() {
        binding.skeletonUi.visibility = View.VISIBLE
        binding.mainUi.visibility = View.GONE

        theme = cleverTapDefaultInstance!!.defineVariable("theme", "loyalty")

        val varNames = listOf("test_var_string", "test_var_string2", "test_var_string3", "test_var_string4", "test_var_string5", "test_var_string6")
        testVars = varNames.mapIndexed { index, name ->
            cleverTapDefaultInstance!!.defineVariable(name, "This is product experiences new testing$index")
        }

        cleverTapDefaultInstance!!.syncVariables()

        cleverTapDefaultInstance!!.fetchVariables {
            val values = varNames.map { name ->
                cleverTapDefaultInstance!!.getVariableValue(name).toString()
            }
            val themeValueFetched = cleverTapDefaultInstance!!.getVariableValue("theme").toString()
            Log.d(TAG, "Values: $values")
            Log.d(TAG, "ThemeValues: $themeValueFetched")

            runOnUiThread {
                renderContent(values, themeValueFetched)
                binding.skeletonUi.visibility = View.GONE
                binding.mainUi.visibility = View.VISIBLE
            }
        }
    }

    private fun renderContent(values: List<String>, themeValue: String) {
        if (themeValue == "loyalty") {
            renderCarousel(values[0])
            renderRestaurant(values[1])
            renderMerchant(values[2])
        } else if (themeValue == "e-commerce") {
            Toast.makeText(applicationContext, themeValue, Toast.LENGTH_SHORT).show()
//            renderCarousel(values[3])
//            renderRestaurant(values[4])
            renderCarousel(values[0])
            renderRestaurant(values[1])
            renderMerchant(values[5])
        }
    }

    private fun renderCarousel(value: String) {
        renderItems(
            value,
            keysProvider = { index -> listOf("image_url_$index", "title$index", "subtitle$index") },
            itemBuilder = { values -> RecyclerViewItem.Carousel(values[0], values[1], values[2]) },
            setupRecyclerView = { items ->
                val capsules = MutableList(items.size) { RecyclerViewItem.Capsule(isSelected = it == 0) }
                val genericAdapter = GenericAdapter(capsules) { item ->
                    val position = capsules.indexOf(item)
                    binding.carouselViewPager.currentItem = position
                }

                binding.capsuleRecyclerView.apply {
                    adapter = genericAdapter
                    layoutManager = LinearLayoutManager(this@ProductExperiencesNewActivity, LinearLayoutManager.HORIZONTAL, false)
                }

                binding.carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        capsules.forEachIndexed { index, item ->
                            capsules[index] = item.copy(isSelected = index == position)
                        }
                        genericAdapter.notifyDataSetChanged()
                    }
                })

                binding.carouselViewPager.adapter = GenericAdapter(items) { item ->
                    Toast.makeText(applicationContext, "$item", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun renderMerchant(value: String) {
        renderItems(
            value,
            keysProvider = { index -> listOf("logo_image_url_$index", "logo_name_$index") },
            itemBuilder = { values -> RecyclerViewItem.Merchant(values[0], values[1]) },
            setupRecyclerView = { items -> setupRecyclerView(binding.merchantsRecyclerView, items) }
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
}
