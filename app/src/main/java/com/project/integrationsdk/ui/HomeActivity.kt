package com.project.integrationsdk.ui

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.BaseActivity
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivityHomeBinding
import com.project.integrationsdk.databinding.ItemCardBinding
import com.project.integrationsdk.databinding.ItemCarouselBinding
import com.project.integrationsdk.session.SessionManager
import kotlin.math.abs

class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val ctInstance by lazy {
        CleverTapAPI.getDefaultInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CleverTapAPI.setDebugLevel(3)
        setupViewPager()
        setupBottomNav()
    }

    private fun setupViewPager() {

        val adapter = object : FragmentStateAdapter(this) {

            override fun getItemCount() = 3

            override fun createFragment(position: Int) =
                when (position) {
                    0 -> HomeFragment()
                    1 -> ProfileFragment()
                    else -> SettingsFragment()
                }
        }

        binding.mainViewPager.adapter = adapter

        // When swiping → update bottom nav
        binding.mainViewPager.registerOnPageChangeCallback(
            object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    binding.bottomNav.menu.getItem(position).isChecked = true
                }
            }
        )
    }

    private fun setupBottomNav() {

        binding.bottomNav.setOnItemSelectedListener {

            when (it.itemId) {
                R.id.nav_home -> binding.mainViewPager.currentItem = 0
                R.id.nav_profile -> binding.mainViewPager.currentItem = 1
                R.id.nav_settings -> binding.mainViewPager.currentItem = 2
            }

            true
        }
    }
}