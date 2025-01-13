package com.project.integrationsdk.ui

import android.os.Bundle
import android.util.Log
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
import java.util.ArrayList

class ProductExperiencesNewActivity : AppCompatActivity() {

    lateinit var binding: ActivityProductExperiencesNewBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    lateinit var testVar1: Var<String>
    lateinit var testVar2: Var<String>
    lateinit var testVar3: Var<String>
    private val TAG = "ProductExpNewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductExperiencesNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        callProductExperienceNew()
    }

    private fun callProductExperienceNew() {
        testVar1 = cleverTapDefaultInstance!!.defineVariable("test_var_string","This is product experiences new testing")
        testVar2 = cleverTapDefaultInstance!!.defineVariable("test_var_string2","This is product experiences new testing2")
        testVar3 = cleverTapDefaultInstance!!.defineVariable("test_var_string3","This is product experiences new testing3")

        cleverTapDefaultInstance!!.syncVariables()

        Toast.makeText(applicationContext, "callProductExperienceNew called", Toast.LENGTH_SHORT).show()

        cleverTapDefaultInstance!!.fetchVariables {
            val value = cleverTapDefaultInstance!!.getVariableValue("test_var_string").toString()
            val value2 = cleverTapDefaultInstance!!.getVariableValue("test_var_string2").toString()
            val value3 = cleverTapDefaultInstance!!.getVariableValue("test_var_string3").toString()
            Log.d(TAG, "Value: $value")
            Log.d(TAG, "Value2: $value2")
            Log.d(TAG, "Value3: $value3")

            runOnUiThread {
                renderCarousel(value)
                renderRestaurant(value2)
                renderMerchant(value3)
            }
        }
    }

    private fun renderCarousel(value: String) {
        renderItems(
            value = value,
            keysProvider = { index ->
                listOf("image_url_$index", "title$index", "subtitle$index")
            },
            itemBuilder = { values ->
                RecyclerViewItem.Carousel(values[0], values[1], values[2])
            },
            setupRecyclerView = { items ->
                val capsules = MutableList(items.size) { RecyclerViewItem.Capsule(isSelected = it == 0) }
                val genericAdapter = GenericAdapter(
                    items = capsules,
                    onClick = { item ->
                        val position = capsules.indexOf(item)
                        binding.carouselViewPager.currentItem = position
                    }
                )
                binding.capsuleRecyclerView.adapter = genericAdapter
                binding.capsuleRecyclerView.layoutManager =
                    LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                binding.carouselViewPager.registerOnPageChangeCallback(object :
                    ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        capsules.forEachIndexed { index, item ->
                            capsules[index] = item.copy(isSelected = index == position)
                        }
                        genericAdapter.notifyDataSetChanged()
                    }
                })

                val carouselAdapter = GenericAdapter(
                    items = items,
                    onClick = { item ->
                        Toast.makeText(applicationContext, "$item.", Toast.LENGTH_SHORT).show()
                    }
                )
                binding.carouselViewPager.adapter = carouselAdapter
            }
        )
    }

    private fun renderMerchant(value: String) {
        renderItems(
            value = value,
            keysProvider = { index ->
                listOf("logo_image_url_$index", "logo_name_$index")
            },
            itemBuilder = { values ->
                RecyclerViewItem.Merchant(values[0], values[1])
            },
            setupRecyclerView = { items ->
                setupRecyclerView(binding.merchantsRecyclerView, items)
            }
        )
    }

    private fun renderRestaurant(value: String) {
        renderItems(
            value = value,
            keysProvider = { index ->
                listOf("restaurant_image_url_$index", "restaurant_name_$index", "restaurant_rating_$index", "restaurant_distance_$index", "restaurant_offer_$index")
            },
            itemBuilder = { values ->
                RecyclerViewItem.Restaurant(
                    values[0], values[1], values[2], values[3], values[4]
                )
            },
            setupRecyclerView = { items ->
                setupRecyclerView(binding.restaurantRecyclerView, items)
            }
        )
    }

    private fun <T> renderItems(value: String, keysProvider: (index: Int) -> List<String>, itemBuilder: (values: List<String>) -> T, setupRecyclerView: (List<T>) -> Unit) {
        val items: MutableList<T> = ArrayList()
        val peValues: JSONObject? = try {
            JSONObject(value)
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON: $e")
            null
        }

        if (peValues == null) {
            Log.e(TAG, "peValues is null, cannot render items")
            return
        }

        var i = 1
        while (true) {
            val keys = keysProvider(i)
            if (keys.all { peValues.has(it) }) {
                val values = keys.map { peValues.getString(it) }
                items.add(itemBuilder(values))
            } else {
                break
            }
            i++
        }

        setupRecyclerView(items)
    }

    private fun <T : RecyclerViewItem> setupRecyclerView(recyclerView: RecyclerView, items: List<T>) {
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val adapter = GenericAdapter(items) { item ->
            when (item) {
                is RecyclerViewItem.Merchant -> {
                    Toast.makeText(recyclerView.context,"Clicked on Merchant: ${item.name}", Toast.LENGTH_SHORT).show()
                }

                is RecyclerViewItem.Restaurant -> {
                    Toast.makeText(recyclerView.context,"Clicked on Restaurant: ${item.name}", Toast.LENGTH_SHORT).show()
                }

                is RecyclerViewItem.Carousel -> {
                    Toast.makeText(recyclerView.context, "Clicked on Carousel", Toast.LENGTH_SHORT).show()
                }

                is RecyclerViewItem.Capsule -> {
                    Toast.makeText(recyclerView.context, "Clicked on Capsule", Toast.LENGTH_SHORT).show()
                }
            }
        }
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }
}