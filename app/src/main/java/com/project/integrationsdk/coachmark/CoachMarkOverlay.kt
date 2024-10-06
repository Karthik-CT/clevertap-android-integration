package com.project.integrationsdk.coachmark

import android.content.Context
import android.graphics.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.project.integrationsdk.R
import com.project.integrationsdk.databinding.CoachmarkitemCoachmarkBinding
import kotlin.math.roundToInt

class CoachMarkOverlay : FrameLayout {

    var mBuilder: Builder? = null
    private var mContext: Context? = context
    private var mBaseBitmap: Bitmap? = null
    private var mLayer: Canvas? = null
    private val mOverlayTintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mOverlayTransparentPaint = Paint()

    private val binding =
        CoachmarkitemCoachmarkBinding.inflate(LayoutInflater.from(mContext), this, true)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, builder: Builder) : super(context) {
        init()
        mBuilder = builder
    }

    private fun init() {
        binding.view.visibility = View.VISIBLE
        binding.view.setOnClickListener(null)
        this.setWillNotDraw(false)
        mOverlayTransparentPaint.color = Color.TRANSPARENT
        mOverlayTransparentPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        // hide
        binding.btnNext.setOnClickListener {
            mBuilder?.getOverlayClickListener()?.apply {
                onOverlayClick(this@CoachMarkOverlay, binding)
            }
        }

        binding.btnSkip.setOnClickListener {
            mBuilder?.getSkipClickListener()?.apply {
                onSkipClick(this@CoachMarkOverlay)
                binding.view.visibility = View.GONE
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawOverlayTint()
        drawTransparentOverlay()
        mBaseBitmap?.apply { canvas.drawBitmap(this, 0f, 0f, null) }
        super.onDraw(canvas)
    }

    private fun drawOverlayTint() = mBuilder?.apply {
        mBaseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mBaseBitmap?.apply {
            mLayer = Canvas(this)
            mOverlayTintPaint.color = getOverlayColor()
            val alpha = getOverlayOpacity()
            mOverlayTintPaint.alpha = alpha
            mLayer?.drawRect(Rect(0, 0, width, height), mOverlayTintPaint)
        }
    }

    // master mine
    private fun drawTransparentOverlay() = mBuilder?.apply {
        mLayer?.let { layer ->
            val targetViewSize = Rect()
            if (getOverlayTargetView() != null) {
                getOverlayTargetView()?.getGlobalVisibleRect(targetViewSize)
            } else {
                targetViewSize.set(getOverlayTargetCoordinates())
            }
            targetViewSize.left -= getOverlayTransparentPadding().left
            targetViewSize.top -= getOverlayTransparentPadding().top
            targetViewSize.right += getOverlayTransparentPadding().right
            targetViewSize.bottom += getOverlayTransparentPadding().bottom

            targetViewSize.left += getOverlayTransparentMargin().left
            targetViewSize.top += getOverlayTransparentMargin().top
            targetViewSize.right += getOverlayTransparentMargin().right
            targetViewSize.bottom += getOverlayTransparentMargin().bottom

            val layerWidth = layer.width
            val layerHeight = layer.height
            when (getOverlayTransparentShape()) {
                Shape.BOX -> {
                    layer.drawRoundRect(
                        RectF(targetViewSize),
                        getOverlayTransparentCornerRadius(),
                        getOverlayTransparentCornerRadius(),
                        mOverlayTransparentPaint
                    )

                    val halfWidthScreen = layerWidth / 2
                    val halfHeightScreen = layerHeight / 2

                    val startMiddleEnd = positionLeftMiddleRight(
                        targetViewSize.left,
                        targetViewSize.right,
                        halfWidthScreen
                    )
                    val topBottom = positionTopBottom(targetViewSize.bottom, halfHeightScreen)

                    binding.apply {
                        txvTitle.text = getTitle()
                        txvSubTitle.text = getSubTitle()
                        if (getMax() == 0) {
                            txvLimit.visibility = View.GONE
                        } else {
                            txvLimit.apply {
                                text = mContext?.getString(
                                    R.string.coachmarkLabel_value_limit,
                                    getLimit().plus(1).toString(),
                                    getMax().plus(1).toString()
                                )
                                visibility = View.VISIBLE
                            }
                        }
                        btnNext.text = getTextBtnPositive()
                        btnNext.setBackgroundColor(getTextBtnPositiveBGColor())
                        btnNext.setTextColor(getTextBtnPositiveTextColor())
                        if (getSkipBtn() == null) {
                            btnSkip.visibility = View.GONE
                        } else {
                            btnSkip.text = getSkipBtn()
                            btnSkip.setBackgroundColor(getSkipBtnBGColor())
                            btnSkip.setTextColor(getSkipBtnTextColor())
                        }
                        if (getGravity() == Gravity.NULL) {
                            // automatic
                            when (topBottom) {
                                GravityIn.TOP -> {
                                    when (startMiddleEnd) {
                                        GravityIn.START -> {
                                            GravityHelper.EndBottomGravity()
                                                .gravity(this, targetViewSize)
                                        }

                                        GravityIn.CENTER -> {
                                            GravityHelper.BottomGravity()
                                                .gravity(this, targetViewSize)
                                        }

                                        GravityIn.END -> {
                                            GravityHelper.StartBottomGravity()
                                                .gravity(this, targetViewSize)
                                        }

                                        else -> {}
                                    }
                                }

                                GravityIn.BOTTOM -> {
                                    when (startMiddleEnd) {
                                        GravityIn.START -> {
                                            GravityHelper.EndTopGravity()
                                                .gravity(this, targetViewSize)
                                        }

                                        GravityIn.CENTER -> {
                                            GravityHelper.TopGravity().gravity(this, targetViewSize)
                                        }

                                        GravityIn.END -> {
                                            GravityHelper.StartTopGravity()
                                                .gravity(this, targetViewSize)
                                        }

                                        else -> {}
                                    }
                                }

                                GravityIn.CENTER -> {
                                    if (startMiddleEnd == GravityIn.CENTER) {
                                        GravityHelper.BottomGravity().gravity(this, targetViewSize)
                                    }
                                }

                                else -> {}
                            }
                        } else {
                            // manual
                            when (getGravity()) {
                                Gravity.TOP -> {
                                    GravityHelper.TopGravity().gravity(this, targetViewSize)
                                }

                                Gravity.BOTTOM -> {
                                    GravityHelper.BottomGravity().gravity(this, targetViewSize)
                                }

                                Gravity.START_TOP -> {
                                    GravityHelper.StartTopGravity().gravity(this, targetViewSize)
                                }

                                Gravity.END_TOP -> {
                                    GravityHelper.EndTopGravity().gravity(this, targetViewSize)
                                }

                                Gravity.END_BOTTOM -> {
                                    GravityHelper.EndBottomGravity().gravity(this, targetViewSize)
                                }

                                Gravity.START_BOTTOM -> {
                                    GravityHelper.StartBottomGravity().gravity(this, targetViewSize)
                                }

                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun positionLeftMiddleRight(left: Int, right: Int, halfWidth: Int): GravityIn {
        return if (halfWidth in left..right) {
            GravityIn.CENTER
        } else if (left in 1 until halfWidth) {
            GravityIn.START
        } else {
            GravityIn.END
        }
    }

    private fun positionTopBottom(bottom: Int, halfHeight: Int): GravityIn {
        return if (bottom in 1 until halfHeight) {
            GravityIn.TOP
        } else {
            GravityIn.BOTTOM
        }
    }

    fun show(root: ViewGroup) {
        root.addView(this)
    }

    class Builder(private val mContext: Context) {
        private var mOverlayTargetView: View? = null
        private var mOverlayColor: Int = Color.BLACK
        private var mOverlayOpacity: Int = 150
        private var mOverlayTransparentShape: Shape = Shape.BOX
        private var mOverlayTransparentCornerRadius: Float =
            mContext.resources.getDimension(R.dimen.margin_07)
        private var mOverlayTransparentMargin: Rect = Rect()
        private var mOverlayTransparentPadding: Rect = Rect()
        private var mOverlayClickListener: OverlayClickListener? = null
        private var mSkipClickListener: SkipClickListener? = null
        private var mTargetCoordinates: Rect = Rect()
        private var mBaseTabPosition: Int = -1
        private var mSetTitle: String = ""
        private var mSetSubTitle: String = ""
        private var mLimit: Int = 0
        private var mTextBtnPositive: String = ""
        private var mSkipBtn: String? = null
        private var mGravity: Gravity = Gravity.NULL
        private var mMax = 0

        private var mTextBtnPositiveBGColor: Int? = null // Store the color
        private var mTextBtnPositiveTextColor: Int? = null // Store the color
        private var mSkipBtnBGColor: Int? = null // Store the color
        private var mSkipBtnTextColor: Int? = null // Store the color

        fun getOverlayTargetView(): View? = mOverlayTargetView
        fun getOverlayColor(): Int = mOverlayColor
        fun getOverlayOpacity(): Int = mOverlayOpacity
        fun getOverlayTransparentShape(): Shape = mOverlayTransparentShape
        fun getOverlayTransparentCornerRadius(): Float = mOverlayTransparentCornerRadius
        fun getTabPosition(): Int = mBaseTabPosition
        fun getOverlayTransparentMargin(): Rect = mOverlayTransparentMargin
        fun getOverlayTransparentPadding(): Rect = mOverlayTransparentPadding
        fun getOverlayTargetCoordinates(): Rect = mTargetCoordinates
        fun getOverlayClickListener(): OverlayClickListener? = mOverlayClickListener
        fun getSkipClickListener(): SkipClickListener? = mSkipClickListener
        fun getTitle(): String = mSetTitle
        fun getSubTitle(): String = mSetSubTitle
        fun getLimit(): Int = mLimit
        fun getTextBtnPositive(): String = mTextBtnPositive
        fun getSkipBtn(): String? = mSkipBtn
        fun getGravity(): Gravity = mGravity
        fun getMax(): Int = mMax

        fun getTextBtnPositiveBGColor(): Int = mTextBtnPositiveBGColor!!
        fun getSkipBtnBGColor(): Int = mSkipBtnBGColor!!
        fun getTextBtnPositiveTextColor(): Int = mTextBtnPositiveTextColor!!
        fun getSkipBtnTextColor(): Int = mSkipBtnTextColor!!

        fun setOverlayTargetCoordinates(coordinates: Rect): Builder {
            mTargetCoordinates.set(coordinates)
            return this
        }

        fun setOverlayTargetView(view: View?): Builder {
            mOverlayTargetView = view
            return this
        }

        fun setTabPosition(position: Int): Builder {
            mBaseTabPosition = position
            return this
        }

        fun setOverlayTransparentPadding(left: Int, top: Int, right: Int, bottom: Int): Builder {
            mOverlayTransparentPadding.left = Utils.dpToPx(mContext, left).roundToInt()
            mOverlayTransparentPadding.top = Utils.dpToPx(mContext, top).roundToInt()
            mOverlayTransparentPadding.right = Utils.dpToPx(mContext, right).roundToInt()
            mOverlayTransparentPadding.bottom = Utils.dpToPx(mContext, bottom).roundToInt()
            return this
        }

        fun setOverlayClickListener(listener: OverlayClickListener): Builder {
            mOverlayClickListener = listener
            return this
        }

        fun setSkipClickListener(listener: SkipClickListener): Builder {
            mSkipClickListener = listener
            return this
        }

        fun setInfoText(title: String, subTitle: String, limit: Int): Builder {
            mSetTitle = title
            mSetSubTitle = subTitle
            mLimit = limit
            return this
        }

        fun setTextBtnPositive(text: String, bgColor: Int? = null, textColor: Int? = null): Builder {
            mTextBtnPositive = text
            mTextBtnPositiveBGColor = bgColor
            mTextBtnPositiveTextColor = textColor
            return this
        }

        fun setSkipBtn(text: String?, bgColor: Int? = null, textColor: Int? = null): Builder {
            mSkipBtn = text
            mSkipBtnBGColor = bgColor
            mSkipBtnTextColor = textColor
            return this
        }

        fun setGravity(gravity: Gravity): Builder {
            mGravity = gravity
            return this
        }

        fun build(max: Int): CoachMarkOverlay {
            mMax = max
            return CoachMarkOverlay(mContext, this)
        }
    }
}