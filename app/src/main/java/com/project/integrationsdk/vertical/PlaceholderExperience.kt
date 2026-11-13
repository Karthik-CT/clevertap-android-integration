package com.project.integrationsdk.vertical

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Stand-in for verticals that don't have their own UI yet. */
class PlaceholderExperience(
    private val host: ExperienceHost,
    private val vertical: Vertical,
) : VerticalExperience {

    override val updates: StateFlow<Any> = MutableStateFlow(Unit)

    override fun setup() {
        host.bottomNav.visibility = View.GONE
    }

    override fun render(force: Boolean) {
        val color = Color.parseColor(vertical.colorHex)
        host.root.setBackgroundColor(color)
        host.swipeRefresh.setColorSchemeColors(color)
        host.content.removeAllViews()
        host.content.addView(LinearLayout(host.context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            addView(label(vertical.emoji, 56f, bold = false))
            addView(label(vertical.title, 24f, bold = true))
            addView(label("UI coming soon", 14f, bold = false).apply { alpha = 0.8f })
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, host.context.resources.displayMetrics.heightPixels))
    }

    override fun fetch(onDone: (Boolean) -> Unit) = onDone(true)

    override fun updateInboxBadge() = Unit

    private fun label(text: String, sp: Float, bold: Boolean) = TextView(host.context).apply {
        this.text = text
        textSize = sp
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }
}
