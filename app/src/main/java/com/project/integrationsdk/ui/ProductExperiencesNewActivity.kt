package com.project.integrationsdk.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.variables.Var
import com.project.integrationsdk.adapter.CapsuleAdapter
import com.project.integrationsdk.adapter.CarouselAdapter
import com.project.integrationsdk.databinding.ActivityProductExperiencesNewBinding
import com.project.integrationsdk.model.CapsuleItem
import com.project.integrationsdk.model.CarouselItem


class ProductExperiencesNewActivity : AppCompatActivity() {

    lateinit var binding: ActivityProductExperiencesNewBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    lateinit var testVar1: Var<String>
    private val TAG = "ProductExpNewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductExperiencesNewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        callProductExperienceNew()

        renderCarousel()

    }

    private fun renderCarousel() {
        val items: MutableList<CarouselItem> = ArrayList()
        items.add(
            CarouselItem(
                "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_1280.jpg",
                "McDonald's",
                "2 McMenu Texas, CBO or Big Tasty for 10.99$"
            )
        )
        items.add(
            CarouselItem(
                "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736881_640.jpg",
                "Starbucks",
                "Buy 3 get 1 free on all Frappuccinos"
            )
        )
        items.add(
            CarouselItem(
                "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_1280.jpg",
                "McDonald's 2",
                "2 McMenu Texas, CBO or Big Tasty for 10.99$"
            )
        )
        items.add(
            CarouselItem(
                "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736881_640.jpg",
                "Starbucks 2",
                "Buy 3 get 1 free on all Frappuccinos"
            )
        )

        val capsules = MutableList(items.size) { CapsuleItem(isSelected = it == 0) }
        val capsuleAdapter = CapsuleAdapter(capsules) { position ->
            binding.carouselViewPager.currentItem = position
        }

        binding.capsuleRecyclerView.adapter = capsuleAdapter
        binding.capsuleRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                capsules.forEachIndexed { index, item ->
                    capsules[index] = item.copy(isSelected = index == position)
                }
                capsuleAdapter.notifyDataSetChanged()
            }
        })


        val adapter = CarouselAdapter(items)
        binding.carouselViewPager.adapter = adapter

    }

    private fun callProductExperienceNew() {
        testVar1 = cleverTapDefaultInstance!!.defineVariable(
            "test_var_string",
            "This is product experiences new testing"
        )

        cleverTapDefaultInstance!!.syncVariables()

        Toast.makeText(applicationContext, "callProductExperienceNew called", Toast.LENGTH_SHORT)
            .show()

        cleverTapDefaultInstance!!.fetchVariables {
            val value = cleverTapDefaultInstance!!.getVariableValue("test_var_string").toString()
            Log.d(TAG, "Value: $value")

//            runOnUiThread {
//                binding.peTextView1.text = value
//            }
        }
    }
}