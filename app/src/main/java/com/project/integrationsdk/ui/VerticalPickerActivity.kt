package com.project.integrationsdk.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.project.integrationsdk.databinding.ActivityVerticalPickerBinding
import com.project.integrationsdk.databinding.ItemVerticalBinding
import com.project.integrationsdk.vertical.Vertical

/** Lets the presenter pick which industry vertical's demo UI to load. */
class VerticalPickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerticalPickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerticalPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, binding.root).isAppearanceLightStatusBars = false

        val toolbarPaddingTop = binding.toolbarContainer.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbarContainer.setPadding(
                binding.toolbarContainer.paddingLeft, toolbarPaddingTop + bars.top,
                binding.toolbarContainer.paddingRight, binding.toolbarContainer.paddingBottom
            )
            binding.verticalList.setPadding(
                binding.verticalList.paddingLeft, 0, binding.verticalList.paddingRight, bars.bottom
            )
            insets
        }

        binding.btnBack.setOnClickListener { finish() }

        Vertical.values().forEach { v ->
            val item = ItemVerticalBinding.inflate(layoutInflater, binding.verticalList, false)
            val color = Color.parseColor(v.colorHex)
            item.accentBar.setBackgroundColor(color)
            item.icon.text = v.emoji
            item.icon.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ColorUtils.setAlphaComponent(color, 0x26))
            }
            item.title.text = v.title
            item.subtitle.text = if (v.hasOwnUi) v.tagline else "${v.tagline} · coming soon"
            item.root.setOnClickListener {
                startActivity(Intent(this, ProductExperiencesNewActivity::class.java).putExtra(Vertical.EXTRA_VERTICAL, v.name))
            }
            binding.verticalList.addView(item.root)
        }
    }
}
