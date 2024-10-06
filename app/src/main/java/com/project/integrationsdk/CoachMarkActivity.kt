package com.project.integrationsdk

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.integrationsdk.coachmark.CoachMarkSequence
import com.project.integrationsdk.coachmark.Gravity
import com.project.integrationsdk.databinding.ActivityCoachMarkBinding

class CoachMarkActivity : AppCompatActivity() {

    lateinit var binding: ActivityCoachMarkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoachMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val coachMarkSequence = CoachMarkSequence(this)
        coachMarkSequence.apply {
            addItem(
                targetView = binding.txvTop,
                title = getString(R.string.title_top),
                subTitle = getString(R.string.lorem_ipsum_text),
                positiveButtonText = "Go Next",
                positiveButtonBGColor = Color.RED,
                positiveButtonTextColor = Color.WHITE,
                skipButtonText = "Go Skip",
                skipButtonBGColor = Color.CYAN,
                skipButtonTextColor = Color.BLACK,
            )
            addItem(
                targetView = binding.txvStartTop,
                title = getString(R.string.title_start_top),
                subTitle = getString(R.string.lorem_ipsum_text),
                gravity = Gravity.END_BOTTOM, positiveButtonText = "Go Next",
                positiveButtonBGColor = Color.RED,
                positiveButtonTextColor = Color.WHITE,
                skipButtonText = "Go Skip",
                skipButtonBGColor = Color.CYAN,
                skipButtonTextColor = Color.BLACK,
            )
            addItem(
                binding.txvEndTop,
                getString(R.string.title_end_top),
                getString(R.string.lorem_ipsum_text), positiveButtonText = "Go Next",
                positiveButtonBGColor = Color.RED,
                positiveButtonTextColor = Color.WHITE,
                skipButtonText = "Go Skip",
                skipButtonBGColor = Color.CYAN,
                skipButtonTextColor = Color.BLACK,
            )
            addItem(
                binding.txvEndBottom,
                getString(R.string.title_end_bottom),
                getString(R.string.lorem_ipsum_text), positiveButtonText = "Go Next",
                positiveButtonBGColor = Color.RED,
                positiveButtonTextColor = Color.WHITE,
                skipButtonText = "Go Skip",
                skipButtonBGColor = Color.CYAN,
                skipButtonTextColor = Color.BLACK,
            )
            addItem(
                binding.txvStartBottom,
                getString(R.string.title_start_bottom),
                getString(R.string.lorem_ipsum_text), positiveButtonText = "Go Next",
                positiveButtonBGColor = Color.RED,
                positiveButtonTextColor = Color.WHITE,
                skipButtonText = "Go Skip",
                skipButtonBGColor = Color.CYAN,
                skipButtonTextColor = Color.BLACK,
            )
            addItem(
                targetView = binding.txvBottom,
                title = getString(R.string.title_bottom),
                subTitle = getString(R.string.lorem_ipsum_text),
                positiveButtonText = getString(R.string.label_btn_explore),
                positiveButtonBGColor = Color.RED,
                positiveButtonTextColor = Color.WHITE,
                skipButtonText = null,

                )
            start(window?.decorView as ViewGroup)
            setOnFinishCallback {
                Toast.makeText(
                    this@CoachMarkActivity,
                    getString(R.string.label_finish), Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}