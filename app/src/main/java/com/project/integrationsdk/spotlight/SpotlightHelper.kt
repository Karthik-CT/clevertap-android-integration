package com.project.integrationsdk.spotlight

import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.project.integrationsdk.R
import com.project.integrationsdk.spotlight.effect.RippleEffect
import com.project.integrationsdk.spotlight.shape.Circle
import org.json.JSONObject

class SpotlightHelper {

    fun showSpotlight(anyActivity: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        val targets = ArrayList<Target>()

        // Get the spotlight count from the JSON object
        val spotlightCount = unit.getJSONObject("custom_kv").getInt("nd_spotlightCount")
        val textColor = unit.getJSONObject("custom_kv").getString("nd_textColor")

        for (i in 1..spotlightCount) {
            val title = unit.getJSONObject("custom_kv").getString("nd_title$i")
            val subtitle = unit.getJSONObject("custom_kv").getString("nd_subTitle$i")
            val anchorId = unit.getJSONObject("custom_kv").getString("nd_title${i}_id")

            if (title.isNotEmpty() && anchorId.isNotEmpty()) {
                val target = createTarget(anyActivity, anchorId, title, subtitle, textColor)
                targets.add(target)
            }
        }

        if (targets.isEmpty()) {
            Log.e("SpotlightHelper", "No valid targets found for spotlight.")
            return // Early return if no targets are available
        }

        val spotlight = Spotlight.Builder(anyActivity)
            .setTargets(targets)
            .setBackgroundColorRes(R.color.spotlightBackground)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {
                    Log.d("SpotlightHelper", "Spotlight started")
                }

                override fun onEnded() {
                    Log.d("SpotlightHelper", "Spotlight ended")
                    onComplete() // Call the completion callback when spotlight ends
                }
            })
            .build()

        spotlight.start()

        val nextTarget = View.OnClickListener { spotlight.next() }
        targets.forEach { target ->
            val overlayView = target.overlay
            overlayView?.setOnClickListener(nextTarget)
        }
    }

    private fun createTarget(
        activity: AppCompatActivity,
        anchorId: String,
        title: String,
        subtitle: String? = null,
        textColor: String
    ): Target {
        val rootView = FrameLayout(activity)
        val overlay = activity.layoutInflater.inflate(R.layout.layout_target, rootView)

        val titleTextView = overlay.findViewById<TextView>(R.id.title_text)
        val subtitleTextView = overlay.findViewById<TextView>(R.id.subtitle_text)

        titleTextView.text = title
        titleTextView.textSize = 20f
        titleTextView.setTypeface(null, Typeface.BOLD)
        titleTextView.setTextColor(Color.parseColor(textColor))
        titleTextView.setPadding(16, 8, 16, 4)

        if (!subtitle.isNullOrEmpty()) {
            subtitleTextView.text = subtitle
            subtitleTextView.textSize = 16f
            subtitleTextView.setTextColor(Color.LTGRAY)
            subtitleTextView.setPadding(16, 4, 16, 8)
            subtitleTextView.visibility = View.VISIBLE
        } else {
            subtitleTextView.visibility = View.GONE
        }

        overlay.viewTreeObserver.addOnGlobalLayoutListener {
            val anchorView = activity.findViewById<View>(activity.resources.getIdentifier(anchorId, "id", activity.packageName))
            val anchorLocation = IntArray(2)
            anchorView.getLocationOnScreen(anchorLocation)
            val anchorCenterX = anchorLocation[0] + anchorView.width / 2
            val anchorCenterY = anchorLocation[1] + anchorView.height / 2

            val screenHeight = activity.resources.displayMetrics.heightPixels
            val screenWidth = activity.resources.displayMetrics.widthPixels
            val circleRadius = 200

            val titleParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.NO_GRAVITY }

            val subtitleParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.NO_GRAVITY }

            if (anchorCenterY < screenHeight / 2) {
                titleParams.topMargin = anchorLocation[1] + circleRadius + 20
                subtitleParams.topMargin = titleParams.topMargin + titleTextView.height + 10
            } else {
                subtitleParams.topMargin = anchorLocation[1] - circleRadius - subtitleTextView.height - 20
                titleParams.topMargin = subtitleParams.topMargin - titleTextView.height - 10
            }

            when {
                anchorCenterX < screenWidth / 3 -> {
                    titleParams.leftMargin = anchorLocation[0]
                    subtitleParams.leftMargin = anchorLocation[0]
                }
                anchorCenterX > screenWidth * 2 / 3 -> {
                    titleParams.leftMargin = anchorLocation[0] - titleTextView.width + anchorView.width
                    subtitleParams.leftMargin = anchorLocation[0] - subtitleTextView.width + anchorView.width
                }
                else -> {
                    titleParams.leftMargin = anchorCenterX - titleTextView.width / 2
                    subtitleParams.leftMargin = anchorCenterX - subtitleTextView.width / 2
                }
            }

            titleTextView.layoutParams = titleParams
            subtitleTextView.layoutParams = subtitleParams
        }

        val anchorView = activity.findViewById<View>(activity.resources.getIdentifier(anchorId, "id", activity.packageName))
        return Target.Builder()
            .setAnchor(anchorView)
            .setShape(Circle(200f))
            .setOverlay(overlay)
            .setEffect(RippleEffect(100f, 200f, Color.argb(30, 124, 255, 90)))
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    Log.d("SpotlightHelper", "$title started")
                }

                override fun onEnded() {
                    Log.d("SpotlightHelper", "$title ended")
                }
            })
            .build()
    }
}