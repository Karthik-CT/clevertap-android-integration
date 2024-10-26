package com.project.integrationsdk

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.databinding.ActivityTooltipsBinding
import com.project.integrationsdk.tooltip.ClosePolicy
import com.project.integrationsdk.tooltip.Tooltip
import com.project.integrationsdk.tooltip.TooltipManager
import com.project.integrationsdk.tooltip.Typefaces
import com.project.integrationsdk.tooltips.ToolTipsSequence

class TooltipsActivity : AppCompatActivity() {

    lateinit var binding: ActivityTooltipsBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null
    lateinit var toolTipsSequence: ToolTipsSequence


    private lateinit var tooltipManager: TooltipManager
    var tooltip: Tooltip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTooltipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolTipsSequence = ToolTipsSequence(this)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        cleverTapDefaultInstance?.pushEvent("ToolsTips Event")

//        callToolTips()

//        Tooltip.Builder(this)
//            .anchor(binding.TextView1, 0, 0, false)
//            .closePolicy(ClosePolicy.TOUCH_ANYWHERE_CONSUME)
//            .showDuration(0)
//            .text("This is a dialog")
//            .create()
//            .show(binding.TextView1, Tooltip.Gravity.TOP, false)


//        tooltip?.dismiss()


        binding.main.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Remove the listener to prevent it from being called multiple times
                binding.main.viewTreeObserver.removeOnGlobalLayoutListener(this)

                // Safe to show the tooltip here

                tooltipManager = TooltipManager(applicationContext)

                // Define your tooltips with background color
                val tooltips = listOf(
                    TooltipManager.TooltipConfig(binding.TextView1, "This is tooltip example 1", Tooltip.Gravity.BOTTOM, Color.parseColor("#FF9800")),
                    TooltipManager.TooltipConfig(binding.TextView2, "This is tooltip example 2", Tooltip.Gravity.LEFT, Color.parseColor("#4CAF50")),
                    TooltipManager.TooltipConfig(binding.TextView3, "This is tooltip example 3", Tooltip.Gravity.RIGHT, Color.parseColor("#2196F3")),
                    TooltipManager.TooltipConfig(binding.TextView4, "This is tooltip example 4", Tooltip.Gravity.TOP, Color.parseColor("#F44336")),
                )

                // Show the tooltips sequentially
                tooltipManager.showTooltipsSequentially(tooltips)

//                Tooltip.Builder(applicationContext)
//                    .anchor(binding.TextView1, 0, 0, false)
//                    .text("this is a tooltip example")
//                    .styleId(R.style.ToolTipAltStyle)
//                    .maxWidth(resources.displayMetrics.widthPixels / 2)
//                    .arrow(true)
//                    .closePolicy(getClosePolicy())
//                    .showDuration(10000)
//                    .overlay(true)
//                    .create()
//                    .doOnHidden {
//                        Tooltip.Builder(applicationContext)
//                            .anchor(binding.TextView2, 0, 0, false)
//                            .text("this is a tooltip example 2")
//                            .styleId(R.style.ToolTipAltStyle)
//                            .maxWidth(resources.displayMetrics.widthPixels / 2)
//                            .arrow(true)
//                            .closePolicy(getClosePolicy())
//                            .showDuration(10000)
//                            .overlay(true)
//                            .create()
//                            .show(binding.TextView2, Tooltip.Gravity.LEFT, true)
//                    }
//                    .show(binding.TextView1, Tooltip.Gravity.BOTTOM, true)
            }
        })
    }

    private fun getClosePolicy(): ClosePolicy {
        val builder = ClosePolicy.Builder()
        builder.inside(true)
        builder.outside(true)
        builder.consume(true)
        return builder.build()
    }


    fun callToolTips() {
        toolTipsSequence.apply {
            addToolTipsItem(
                targetView = binding.TextView1,
                title = "This is a tooltips for title 1",
            )
            addToolTipsItem(
                targetView = binding.TextView2,
                title = "This is a tooltips for title 2",
            )
            addToolTipsItem(
                targetView = binding.TextView3,
                title = "This is a tooltips for title 3",
            )
            addToolTipsItem(
                targetView = binding.TextView4,
                title = "This is a tooltips for title 4",
            )
            start(window?.decorView as ViewGroup)
            setOnFinishCallback {
                Toast.makeText(
                    this@TooltipsActivity,
                    getString(R.string.label_finish), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

//    private fun showTooltipsSequentially(tooltips: List<TooltipConfig>, index: Int = 0) {
//        if (index >= tooltips.size) return // No more tooltips to show
//
//        val config = tooltips[index]
//
//        Tooltip.Builder(applicationContext)
//            .anchor(config.anchorView, 0, 0, false)
//            .text(config.text)
//            .styleId(R.style.ToolTipAltStyle)
//            .maxWidth(resources.displayMetrics.widthPixels / 2)
//            .arrow(true)
//            .closePolicy(getClosePolicy())
//            .showDuration(10000)
//            .overlay(true)
//            .create().doOnHidden {
//                // Show the next tooltip after the current one is hidden
//                showTooltipsSequentially(tooltips, index + 1)
//            }
//            .show(config.anchorView, config.gravity, true)
//
//    }


}