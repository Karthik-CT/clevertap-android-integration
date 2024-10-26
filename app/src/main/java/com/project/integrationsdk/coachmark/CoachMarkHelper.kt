package com.project.integrationsdk.coachmark

import android.graphics.Color
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class CoachMarkHelper {

    lateinit var coachMarkSequence: CoachMarkSequence

    fun renderCoachMark(context: AppCompatActivity, unit: JSONObject, onComplete: () -> Unit) {
        coachMarkSequence = CoachMarkSequence(context)
        coachMarkSequence.apply {
            val coachMarkCount = unit.getJSONObject("custom_kv").getInt("nd_coachMarkCount")
            for (i in 1..coachMarkCount) {
                val titleKey = "nd_title$i"
                val subTitleKey = "nd_subtitle$i"
                val viewId = context.resources.getIdentifier(unit.getJSONObject("custom_kv").getString(titleKey + "_id"), "id", context.packageName)
                val isLastItem = (i == coachMarkCount)
                addCoachMarkItem(viewId, titleKey, subTitleKey, isLastItem, context, unit)
            }

            start(context.window?.decorView as ViewGroup)
            setOnFinishCallback {
                onComplete()
            }
        }
    }

    fun addCoachMarkItem(viewId: Int, titleKey: String, subTitleKey: String, isLastItem: Boolean = false, context: AppCompatActivity, unit: JSONObject, ) {
        coachMarkSequence.addItem(
            targetView = context.findViewById(viewId),
            title = unit.getJSONObject("custom_kv").getString(titleKey),
            subTitle = unit.getJSONObject("custom_kv").getString(subTitleKey),
            positiveButtonText = if (isLastItem) unit.getJSONObject("custom_kv").getString("nd_finalPositiveButtonText") else unit.getJSONObject("custom_kv").getString("nd_positiveButtonText"),
            skipButtonText = if (isLastItem) null else unit.getJSONObject("custom_kv").getString("nd_skipButtonText"),
            positiveButtonTextColor = Color.parseColor(unit.getJSONObject("custom_kv").getString("nd_postiveBtnTextColor")),
            positiveButtonBGColor = Color.parseColor(unit.getJSONObject("custom_kv").getString("nd_postiveBtnBackgroundColor")),
            skipButtonBGColor = if (!isLastItem) Color.parseColor(unit.getJSONObject("custom_kv").getString("nd_skipBtnBackgroundColor")) else Color.TRANSPARENT,
            skipButtonTextColor = if (!isLastItem) Color.parseColor(unit.getJSONObject("custom_kv").getString("nd_skipBtnTextColor")) else Color.TRANSPARENT
        )
    }
}