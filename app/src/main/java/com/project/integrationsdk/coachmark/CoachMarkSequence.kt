package com.project.integrationsdk.coachmark


import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.CoachmarkitemCoachmarkBinding
import java.util.*

class CoachMarkSequence(private val mContext: Activity) {

    private val mSequenceQueue: Queue<CoachMarkOverlay.Builder> = LinkedList()
    var mCoachMark: CoachMarkOverlay? = null
    private var mSequenceItem: CoachMarkOverlay.Builder? = null
    private var onFinishCallback: OnFinishCallback? = null

    private val mCoachMarkSkipButtonClickListener: SkipClickListener =
        object : SkipClickListener {
            override fun onSkipClick(view: View) {
                (mContext.window.decorView as ViewGroup).removeView(view)
                mSequenceQueue.clear()
                onFinishCallback?.onFinish()
            }
        }
    private var mSequenceListener: SequenceListener = object : SequenceListener {}

    private val mCoachMarkOverlayClickListener: OverlayClickListener =
        object : OverlayClickListener {
            override fun onOverlayClick(overlay: CoachMarkOverlay, binding: CoachmarkitemCoachmarkBinding) {
                mCoachMark = overlay
                if (mSequenceQueue.size > 0) {
                    mSequenceItem = mSequenceQueue.poll()
                    if (mSequenceQueue.isEmpty()) {
                        overlay.mBuilder?.setOverlayTransparentPadding(0, 0, 0, 0)
                    }
                    mSequenceItem?.apply {
                        overlay.mBuilder?.let { builder ->
                            builder.setTabPosition(getTabPosition())
                            if (builder.getOverlayTargetView() != null) {
                                builder.setInfoText(getTitle(), getSubTitle(), getLimit())
                                builder.setSkipBtn(getSkipBtn())
                                builder.setTextBtnPositive(getTextBtnPositive())
                                builder.setOverlayTargetView(getOverlayTargetView())
                                builder.setGravity(getGravity())
                            } else {
                                builder.setInfoText("", "", 0)
                                builder.setSkipBtn("null")
                                builder.setTextBtnPositive("")
                                builder.setOverlayTargetView(null)
                                builder.setGravity(Gravity.NULL)
                                builder.setOverlayTargetCoordinates(getOverlayTargetCoordinates())
                            }
                            mSequenceListener.apply {
                                onNextItem(overlay, this@CoachMarkSequence)
                            }
                        }
                    }
                } else {
                    (mContext.window.decorView as ViewGroup).removeView(overlay)
                    mSequenceQueue.clear()
                    binding.view.visibility = View.GONE
                    onFinishCallback?.onFinish()
                }
            }
        }

    // set the data to view the next descriptions.
    fun setNextView() {
        if (mCoachMark != null && mSequenceItem != null) {
            mCoachMark?.invalidate()
        }
    }

    fun addItem(
        targetView: View,
        title: String = "",
        subTitle: String = "",
        positiveButtonText: String = mContext.getString(R.string.coachmarkNext),
        skipButtonText: String? = mContext.getString(R.string.coachmarkSkip),
        gravity: Gravity = Gravity.NULL,
    ) {
        CoachMarkOverlay.Builder(mContext).apply {
            setOverlayClickListener(mCoachMarkOverlayClickListener)
            setSkipClickListener(mCoachMarkSkipButtonClickListener)
            setOverlayTargetView(targetView)
            setInfoText(title, subTitle, mSequenceQueue.size)
            setTextBtnPositive(positiveButtonText)
            setSkipBtn(skipButtonText)
            setGravity(gravity)
            mSequenceQueue.add(this)
        }
    }

    fun start(rootView: ViewGroup? = null) {
        if (mSequenceQueue.size > 0) {
            val firstElement = mSequenceQueue.poll()
            if (rootView == null) {
                firstElement?.build(mSequenceQueue.size)?.show((mContext).window.decorView as ViewGroup)
            } else {
                firstElement?.build(mSequenceQueue.size)?.show(rootView)
            }
        }
    }

    fun setOnFinishCallback(onFinishCallback: OnFinishCallback) {
        this.onFinishCallback = onFinishCallback
    }

    fun interface OnFinishCallback {
        fun onFinish()
    }
}