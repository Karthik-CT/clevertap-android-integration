package com.project.integrationsdk.spotlight

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.app.Activity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

class Spotlight private constructor(
    private val spotlight: SpotlightView,
    private val targets: Array<Target>,
    private val duration: Long,
    private val interpolator: TimeInterpolator,
    private val container: ViewGroup,
    private val spotlightListener: OnSpotlightListener?
) {

    private var currentIndex = NO_POSITION

    init {
        container.addView(spotlight, MATCH_PARENT, MATCH_PARENT)
    }

    fun start() {
        startSpotlight()
    }

    fun show(index: Int) {
        showTarget(index)
    }

    fun next() {
        showTarget(currentIndex + 1)
    }

    fun previous() {
        showTarget(currentIndex - 1)
    }

    fun finish() {
        finishSpotlight()
    }

    private fun startSpotlight() {
        spotlight.startSpotlight(duration, interpolator, object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                spotlightListener?.onStarted()
            }

            override fun onAnimationEnd(animation: Animator) {
                showTarget(0)
            }
        })
    }

    private fun showTarget(index: Int) {
        if (currentIndex == NO_POSITION) {
            val target = targets[index]
            currentIndex = index
            spotlight.startTarget(target)
            target.listener?.onStarted()
        } else {
            spotlight.finishTarget(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val previousIndex = currentIndex
                    val previousTarget = targets[previousIndex]
                    previousTarget.listener?.onEnded()
                    if (index < targets.size) {
                        val target = targets[index]
                        currentIndex = index
                        spotlight.startTarget(target)
                        target.listener?.onStarted()
                    } else {
                        finishSpotlight()
                    }
                }
            })
        }
    }

    private fun finishSpotlight() {
        spotlight.finishSpotlight(duration, interpolator, object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                spotlight.cleanup()
                container.removeView(spotlight)
                spotlightListener?.onEnded()
            }
        })
    }

    companion object {

        private const val NO_POSITION = -1
    }

    class Builder(private val activity: Activity) {

        private var targets: Array<Target>? = null
        private var duration: Long = DEFAULT_DURATION
        private var interpolator: TimeInterpolator = DEFAULT_ANIMATION
        @ColorInt private var backgroundColor: Int = DEFAULT_OVERLAY_COLOR
        private var container: ViewGroup? = null
        private var listener: OnSpotlightListener? = null

        fun setTargets(vararg targets: Target): Builder = apply {
            require(targets.isNotEmpty()) { "targets should not be empty. " }
            this.targets = arrayOf(*targets)
        }

        fun setTargets(targets: List<Target>): Builder = apply {
            require(targets.isNotEmpty()) { "targets should not be empty. " }
            this.targets = targets.toTypedArray()
        }

        fun setDuration(duration: Long): Builder = apply {
            this.duration = duration
        }

        fun setBackgroundColorRes(@ColorRes backgroundColorRes: Int): Builder = apply {
            this.backgroundColor = ContextCompat.getColor(activity, backgroundColorRes)
        }

        fun setBackgroundColor(@ColorInt backgroundColor: Int): Builder = apply {
            this.backgroundColor = backgroundColor
        }

        fun setAnimation(interpolator: TimeInterpolator): Builder = apply {
            this.interpolator = interpolator
        }

        fun setContainer(container: ViewGroup) = apply {
            this.container = container
        }

        fun setOnSpotlightListener(listener: OnSpotlightListener): Builder = apply {
            this.listener = listener
        }

        fun build(): Spotlight {

            val spotlight = SpotlightView(activity, null, 0, backgroundColor)
            val targets = requireNotNull(targets) { "targets should not be null. " }
            val container = container ?: activity.window.decorView as ViewGroup

            return Spotlight(
                spotlight = spotlight,
                targets = targets,
                duration = duration,
                interpolator = interpolator,
                container = container,
                spotlightListener = listener
            )
        }

        companion object {

            private val DEFAULT_DURATION = TimeUnit.SECONDS.toMillis(1)

            private val DEFAULT_ANIMATION = DecelerateInterpolator(2f)

            @ColorInt private val DEFAULT_OVERLAY_COLOR: Int = 0x6000000
        }
    }
}