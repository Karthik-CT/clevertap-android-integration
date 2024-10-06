package com.project.integrationsdk.coachmark


import android.graphics.Rect
import android.view.View
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.CoachmarkitemCoachmarkBinding

object GravityHelper {

    interface GravityListener {
        fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect)
    }

    class TopGravity : GravityListener {
        override fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect) {
            view.apply {
                itemDashed.visibility = View.VISIBLE
                itemDashed.setImageResource(R.drawable.img_dashed_coachmark_bottom)
                val middle = targetViewSize.right.toFloat().minus(targetViewSize.left) / 2
                itemDashed.translationX = targetViewSize.left.plus(middle).minus(18)
                itemDashed.translationY = targetViewSize.top.minus(120).toFloat()
                itemRoot.visibility = View.VISIBLE
                itemRoot.translationY = itemDashed.translationY.minus(itemRoot.height).minus(30)
                itemRoot.translationX = 0f
            }
        }
    }

    class BottomGravity: GravityListener {
        override fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect) {
            view.apply {
                itemDashed.visibility = View.VISIBLE
                val middle = targetViewSize.right.toFloat().minus(targetViewSize.left) / 2
                itemDashed.translationX = targetViewSize.left.plus(middle).minus(18)
                itemDashed.translationY = targetViewSize.bottom.plus(30).toFloat()
                itemDashed.setImageResource(R.drawable.img_dashed_coachmark_top)
                itemRoot.visibility = View.VISIBLE
                itemRoot.translationY = itemDashed.translationY.plus(itemDashed.height).plus(30)
                itemRoot.translationX = 0f
            }
        }
    }

    class StartTopGravity: GravityListener {
        override fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect) {
            view.apply {
                itemDashed.visibility = View.VISIBLE
                itemDashed.setImageResource(R.drawable.img_dashed_coachmark_end_bottom)
                itemDashed.translationX = targetViewSize.left.minus(120).toFloat()
                itemDashed.translationY = (targetViewSize.bottom.minus(itemDashed.bottom)).minus(targetViewSize.bottom.minus(targetViewSize.top).div(2)).toFloat()
                itemRoot.visibility = View.VISIBLE
                itemRoot.translationY = itemDashed.translationY.minus(itemRoot.height).minus(30)
                itemRoot.translationX = 0f
            }
        }
    }

    class EndTopGravity: GravityListener {
        override fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect) {
            view.apply {
                itemDashed.visibility = View.VISIBLE
                itemDashed.setImageResource(R.drawable.img_dashed_coachmark_left_bottom)
                itemDashed.translationX = targetViewSize.right.plus(30).toFloat()
                itemDashed.translationY = (targetViewSize.bottom.minus(itemDashed.height)).minus(targetViewSize.bottom.minus(targetViewSize.top).div(2)).plus(4).toFloat()
                itemRoot.visibility = View.VISIBLE
                itemRoot.translationY = itemDashed.translationY.minus(itemRoot.height).minus(30)
                itemRoot.translationX = 0f
            }
        }
    }

    class EndBottomGravity: GravityListener {
        override fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect) {
            view.apply {
                itemDashed.visibility = View.VISIBLE
                itemDashed.setImageResource(R.drawable.img_dashed_coachmark_left)
                itemDashed.translationX = targetViewSize.right.plus(30).toFloat()
                itemDashed.translationY = targetViewSize.bottom.minus(targetViewSize.bottom.minus(targetViewSize.top).div(2)).toFloat()
                itemRoot.visibility = View.VISIBLE
                itemRoot.translationY = itemDashed.translationY.plus(itemDashed.height).plus(30)
                itemRoot.translationX = 0f
            }
        }
    }

    class StartBottomGravity: GravityListener {
        override fun gravity(view: CoachmarkitemCoachmarkBinding, targetViewSize: Rect) {
            view.apply {
                itemDashed.visibility = View.VISIBLE
                itemDashed.setImageResource(R.drawable.img_dashed_coachmark_right)
                itemDashed.translationX = targetViewSize.left.minus(120).toFloat()
                itemDashed.translationY = targetViewSize.bottom.minus(targetViewSize.bottom.minus(targetViewSize.top).div(2)).toFloat()
                itemRoot.visibility = View.VISIBLE
                itemRoot.translationY = itemDashed.translationY.plus(itemDashed.height).plus(30)
                itemRoot.translationX = 0f
            }
        }
    }

}