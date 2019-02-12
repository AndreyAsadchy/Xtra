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
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.customview.widget.ViewDragHelper
import com.github.exact7.xtra.R
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerView

const val BOTTOM_MARGIN = 75f //before scaling
const val ANIMATION_DURATION = 250L

class SlidingLayout : RelativeLayout {

    private val viewDragHelper = ViewDragHelper.create(this, 1f, SlidingCallback())

    private lateinit var dragView: PlayerView
    private lateinit var timeBar: DefaultTimeBar
    private var secondView: View? = null

    private var topBound = 0
    private var bottomBound = 0
    private var minimizeThreshold = 0
    private var bottomMargin = 0f

    private var dragViewTop = 0
    private var dragViewLeft = 0
    private var minScaleX = 0f
    private var minScaleY = 0f

    private var touchX = 0f
    private var touchY = 0f

    private var isPortrait = true
    private var isMaximized = true
    private var isAnimating = false

    private var callback: Callback? = null
    private val animatorListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) { isAnimating = true }
        override fun onAnimationEnd(animation: Animator?) { isAnimating = false }
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
            if (dragView.height == height) {//landscape
                bottomBound = (height / 1.5f).toInt()
                minScaleX = 0.3f
                minScaleY = 0.3f
                isPortrait = false
            } else  { //portrait
                bottomBound = height / 2
                minScaleX = 0.5f
                minScaleY = 0.5f
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
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (!isAnimating) {
            dragView.layout(dragViewLeft, dragViewTop, r + dragViewLeft, dragView.measuredHeight + dragViewTop)
        }
        if (isMaximized) {
            secondView?.layout(l, dragView.measuredHeight + dragViewTop, r, b + dragViewTop)
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
        dragView.dispatchTouchEvent(event)
        if (isDragViewHit && !isMaximized) {
            maximize()
            return true
        }
        if (timeBar.isPressed) {
            return true
        }
        if (isClick(event)) {
            performClick()
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

    private fun isClick(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
                false
            }
            MotionEvent.ACTION_UP -> touchX == event.x && touchY == event.y && event.eventTime - event.downTime <= 500
            else -> false
        }
    }

    fun maximize() {
        isMaximized = true
        secondView?.requestLayout()
        animate(1f, 1f)
        callback?.onMaximize()
    }

    fun minimize() {
        isMaximized = false
        secondView?.layout(0, 0, 0, 0)
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
                it.top = dragView.height + top
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