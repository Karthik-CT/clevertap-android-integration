package com.project.integrationsdk

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.integrationsdk.coachmark.CoachMarkSequence
import com.project.integrationsdk.coachmark.Gravity
import com.project.integrationsdk.databinding.ActivityRestaurantBinding

class RestaurantActivity : AppCompatActivity() {

    lateinit var binding: ActivityRestaurantBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val coachMarkSequence = CoachMarkSequence(this)
        coachMarkSequence.apply {
            addItem(
                targetView = binding.profileImage,
                title = "This will take you to your profile",
                subTitle = "profile page where you can check your profile",
            )
            addItem(
                targetView = binding.search,
                title = "Search your favorite food here",
                subTitle = "Search your favorite food here",
                gravity = Gravity.END_BOTTOM
            )
            addItem(
                targetView = binding.cart,
                title = "This is your cart",
                "This is your cart"
            )
            addItem(
                targetView = binding.supportHelp,
                title = "Need help? click here to chat with the support team",
                subTitle = "Need help? click here to chat with the support team"
            )
            addItem(
                targetView = binding.settings,
                title = "Want to change settings? click here",
                subTitle = "Want to change settings? click here",
                positiveButtonText = getString(R.string.label_btn_explore),
                skipButtonText = null
            )
            start(window?.decorView as ViewGroup)
            setOnFinishCallback {
                Toast.makeText(
                    this@RestaurantActivity,
                    getString(R.string.label_finish), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

