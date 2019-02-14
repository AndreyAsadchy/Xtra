package com.github.exact7.xtra.ui.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.customview.widget.ViewDragHelper
import com.github.exact7.xtra.R
import com.github.exact7.xtra.util.isClick
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerView

const val BOTTOM_MARGIN = 75f //before scaling
const val ANIMATION_DURATION = 250L

class SlidingLayout : LinearLayout {

    private val viewDragHelper = ViewDragHelper.create(this, 1f, SlidingCallback())

    private lateinit var dragView: PlayerView
    private var secondView: View? = null
    private var timeBar: DefaultTimeBar? = null

    private var topBound = 0
    private var bottomBound = 0
    private var minimizeThreshold = 0
    private var bottomMargin = 0f

    private var dragViewTop = 0
    private var dragViewLeft = 0
    private var minScaleX = 0f
    private var minScaleY = 0f

    private var downTouchLocation = FloatArray(2)

    private val isPortrait: Boolean
        get() = orientation == 1
    private var isMaximized = true
    private var isAnimating = false
    private var shouldUpdateDragLayout = false

    private var callback: Callback? = null
    private val animatorListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) { isAnimating = true }
        override fun onAnimationEnd(animation: Animator?) {
            isAnimating = false
            shouldUpdateDragLayout = false
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        id = R.id.sliding_layout
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        dragView = getChildAt(0) as PlayerView
        secondView = getChildAt(1)
        dragView.post {
            topBound = paddingTop
            minimizeThreshold = height / 5
            if (isPortrait) { //portrait
                bottomBound = height / 2
                minScaleX = 0.5f
                minScaleY = 0.5f
            } else { //landscape
                bottomBound = (height / 1.5f).toInt()
                minScaleX = 0.3f
                minScaleY = 0.3f
            }
            bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BOTTOM_MARGIN / (1f - minScaleY), resources.displayMetrics)
            pivotX = width * 0.95f
            pivotY = if (isPortrait) {
                height * 2 - dragView.height - bottomMargin
            } else {
                height - bottomMargin
            }
            if (!isMaximized) {
                scaleX = minScaleX
                scaleY = minScaleY
            }
            timeBar = dragView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_progress)
        }
        secondView?.post {
            if (!isMaximized) {
                secondView?.visibility = View.GONE
                dragView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val height = dragView.measuredHeight
        if (!isAnimating || shouldUpdateDragLayout) {
            dragView.layout(dragViewLeft, dragViewTop, if (isMaximized) dragView.measuredWidth + dragViewLeft else width, height + dragViewTop)
        }
        secondView?.let {
            if (isMaximized) {
                if (isPortrait) {
                    it.layout(l, height + dragViewTop, r, b + dragViewTop)
                } else {
                    it.layout(dragView.measuredWidth, dragViewTop, width, height + dragViewTop)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                viewDragHelper.cancel()
                return false
            }
            MotionEvent.ACTION_DOWN -> {
                if (ev.getPointerId(ev.actionIndex) == ViewDragHelper.INVALID_POINTER) {
                    return false
                }
            }
        }
        val interceptTap = viewDragHelper.isViewUnder(dragView, ev.x.toInt(), ev.y.toInt())
        return (viewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isAnimating) return true
        val x = event.x.toInt()
        val y = event.y.toInt()
        val isDragViewHit = isViewHit(dragView, x, y)
        val isSecondViewHit = secondView?.let { isViewHit(it, x, y) } == true
        val isClick = event.isClick(downTouchLocation)
        if (isClick) {
            performClick()
        }
        if (isDragViewHit) {
            if (isMaximized) {
                dragView.dispatchTouchEvent(event)
            } else if (isClick) {
                maximize()
                return true
            }
        }
        if (timeBar?.isPressed == true) {
            if (isSecondViewHit) {
                timeBar?.dispatchTouchEvent(event)
            }
            return true
        }
        viewDragHelper.processTouchEvent(event)
        return isDragViewHit || isSecondViewHit
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun isViewHit(view: View, x: Int, y: Int): Boolean {
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return (screenX >= viewLocation[0]
                && screenX < viewLocation[0] + view.width
                && screenY >= viewLocation[1]
                && screenY < viewLocation[1] + view.height)
    }

    fun maximize() {
        isMaximized = true
        secondView?.apply {
            requestLayout()
            if (!isPortrait) {
                shouldUpdateDragLayout = true
                visibility = View.VISIBLE
                dragView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
        animate(1f, 1f)
        callback?.onMaximize()
    }

    fun minimize() {
        isMaximized = false
        secondView?.apply {
            layout(0, 0, 0, 0)
            if (!isPortrait) {
                shouldUpdateDragLayout = true
                visibility = View.GONE
                dragView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            }
        }
        animate(minScaleX, minScaleY)
        if (dragViewTop != 0) {
            dragViewTop = 0
            val top = PropertyValuesHolder.ofInt("top", 0)
            val bot = PropertyValuesHolder.ofInt("bottom", dragView.height)
            ObjectAnimator.ofPropertyValuesHolder(dragView, top, bot).apply {
                duration = ANIMATION_DURATION
                start()
            }
        }
        dragView.hideController()
        callback?.onMinimize()
    }

    private fun animate(scaleX: Float, scaleY: Float) {
        val sclX = PropertyValuesHolder.ofFloat("scaleX", scaleX)
        val sclY = PropertyValuesHolder.ofFloat("scaleY", scaleY)
        ObjectAnimator.ofPropertyValuesHolder(this, sclX, sclY).apply {
            duration = ANIMATION_DURATION
            addListener(animatorListener)
            start()
        }
    }

    private fun smoothSlideTo(left: Int, top: Int) {
        if (viewDragHelper.smoothSlideViewTo(dragView, left, top)) {
            postInvalidateOnAnimation()
        }
    }

    private fun closeTo(left: Int) {
        smoothSlideTo(left, dragViewTop)
        callback?.onClose()
    }

    override fun computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            postInvalidateOnAnimation()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return bundleOf("superState" to super.onSaveInstanceState(), "isMaximized" to isMaximized)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state.let {
            if (it is Bundle) {
                isMaximized = it.getBoolean("isMaximized")
                it.getParcelable("superState")
            } else {
                it
            }
        })
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    private inner class SlidingCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child == dragView
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if (isMaximized) Math.min(Math.max(top, topBound), bottomBound) else child.top
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (!isMaximized) left else child.left
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dragViewTop = top
            dragViewLeft = left
            secondView?.let {
                it.top = if (isPortrait) dragView.measuredHeight + top else top
                it.bottom = height + top
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (isMaximized) {
                when {
                    releasedChild.top >= minimizeThreshold -> minimize()
                    else -> smoothSlideTo(0, 0)
                }
            } else {
                when {
                    xvel > 1500 -> closeTo(width)
                    xvel < -1500 -> closeTo(-dragView.width)
                    else -> smoothSlideTo(0, 0)
                }
            }
        }
    }

    interface Callback {
        fun onMinimize()
        fun onMaximize()
        fun onClose()
    }
}