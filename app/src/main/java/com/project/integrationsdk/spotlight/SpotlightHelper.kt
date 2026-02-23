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
import com.project.integrationsdk.spotlight.shape.Capsule
import com.project.integrationsdk.spotlight.shape.Rectangle
import org.json.JSONException
import org.json.JSONObject

class SpotlightHelper {

    fun showSpotlight(anyActivity: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        try {
            val customKv = unit.optJSONObject("custom_kv")?.apply {
                if (has("nd_json")) {
                    val ndJsonString = optString("nd_json", null)
                    if (!ndJsonString.isNullOrEmpty()) {
                        val parsedNdJson = JSONObject(ndJsonString)
                        Log.d("SpotlightHelper", "Parsed nd_json: $parsedNdJson")
                        for (key in parsedNdJson.keys()) {
                            put(key, parsedNdJson.get(key))
                        }
                    }
                }
            } ?: throw JSONException("Missing 'custom_kv' in unit JSONObject")

            Log.d("SpotlightHelper", "Merged customKv: $customKv")

            val spotlightCount = customKv.getInt("nd_spotlight_count")
            val textColor = customKv.optString("nd_text_color", "#FFFFFF")

            val targets = ArrayList<Target>()
            for (i in 1..spotlightCount) {
                try {
                    val title = customKv.getString("nd_view${i}_title")
                    val subtitle = customKv.optString("nd_view${i}_subtitle", "")
                    val anchorId = customKv.getString("nd_view${i}_id")

                    if (title.isNotEmpty() && anchorId.isNotEmpty()) {
                        val target = createTarget(anyActivity, anchorId, title, subtitle, textColor)
                        targets.add(target)
                    }
                } catch (e: JSONException) {
                    Log.e("SpotlightHelper", "Error parsing spotlight view $i: ${e.message}")
                } catch (e: Exception) {
                    Log.e("SpotlightHelper", "Unexpected error in spotlight view $i: ${e.message}")
                }
            }

            if (targets.isEmpty()) {
                Log.e("SpotlightHelper", "No valid targets found for spotlight.")
                return
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
                        onComplete()
                    }
                })
                .build()

            spotlight.start()

            val nextTarget = View.OnClickListener { spotlight.next() }
            targets.forEach { target ->
                val overlayView = target.overlay
                overlayView?.setOnClickListener(nextTarget)
            }
        } catch (e: JSONException) {
            Log.e("SpotlightHelper", "Error parsing spotlight JSON: ${e.message}")
        } catch (e: Exception) {
            Log.e("SpotlightHelper", "Unexpected error: ${e.message}")
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
//            .setShape(Circle(200f))
//            .setShape(Capsule(200f, 500f, 500f))
//            .setShape(Rectangle(500f, 200f))
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