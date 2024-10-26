package com.project.integrationsdk

import android.graphics.Color
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import com.clevertap.android.sdk.CleverTapAPI
import com.project.integrationsdk.databinding.ActivityTooltipsBinding
import com.project.integrationsdk.tooltip.ClosePolicy
import com.project.integrationsdk.tooltip.Tooltip
import com.project.integrationsdk.tooltip.TooltipManager

class TooltipsActivity : AppCompatActivity() {

    lateinit var binding: ActivityTooltipsBinding
    private var cleverTapDefaultInstance: CleverTapAPI? = null

    private lateinit var tooltipManager: TooltipManager
    var tooltip: Tooltip? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTooltipsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cleverTapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

        cleverTapDefaultInstance?.pushEvent("ToolsTips Event")

        binding.main.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.main.viewTreeObserver.removeOnGlobalLayoutListener(this)
                tooltipManager = TooltipManager(applicationContext)
                val tooltips = listOf(
                    TooltipManager.TooltipConfig(binding.TextView1, "This is tooltip example 1", Tooltip.Gravity.BOTTOM),
                    TooltipManager.TooltipConfig(binding.TextView2, "This is tooltip example 2", Tooltip.Gravity.LEFT),
                    TooltipManager.TooltipConfig(binding.TextView3, "This is tooltip example 3", Tooltip.Gravity.RIGHT),
                    TooltipManager.TooltipConfig(binding.TextView4, "This is tooltip example 4", Tooltip.Gravity.TOP),
                )
                tooltipManager.showTooltipsSequentially(tooltips)
            }
        })
    }
}