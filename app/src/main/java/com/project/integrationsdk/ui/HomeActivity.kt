package com.project.integrationsdk.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Step 1: Draw behind status bar and nav bar ────────────
        // This makes our toolbar gradient extend all the way to the top.
        // We handle the insets manually below so nothing is obscured.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE)

        // ── Step 2: Insets — ONLY pad the fragmentContainer top ───
        // The fragment's toolbar will offset itself by this amount.
        // The BottomNavigationView handles its own bottom inset.
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Bottom nav: pad bottom so it sits above gesture bar / nav buttons
            binding.bottomNav.setPadding(0, 0, 0, bars.bottom)

            insets
        }

        // ── Step 3: Load home fragment ────────────────────────────
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            binding.bottomNav.selectedItemId = R.id.nav_home
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home     -> { loadFragment(HomeFragment());     true }
                R.id.nav_profile  -> { loadFragment(ProfileFragment());  true }
                R.id.nav_settings -> { loadFragment(SettingsFragment()); true }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}