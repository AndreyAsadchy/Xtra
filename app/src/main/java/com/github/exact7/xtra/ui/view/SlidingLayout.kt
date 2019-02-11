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
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper

const val BOTTOM_MARGIN = 75f //before scaling
const val ANIMATION_DURATION = 250L

class SlidingLayout : RelativeLayout {

    private val viewDragHelper = ViewDragHelper.create(this, 1f, SlidingCallback())

    private lateinit var dragView: View
    private var secondView: View? = null

    private var topBound = 0
    private var bottomBound = 0
    private var minimizeThreshold = 0
    private var bottomMargin = 0f

    private var dragViewTop = 0
    private var dragViewLeft = 0
    private var minScaleX = 0f
    private var minScaleY = 0f

    private var clickStartX = 0f
    private var clickStartY = 0f
    private var clickEndX = 0f
    private var clickEndY = 0f
    private var clickDuration = 0L

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

    override fun onFinishInflate() {
        id = ViewCompat.generateViewId() //TODO do something about ids
        super.onFinishInflate()
        dragView = getChildAt(0)
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
            println("MAXIMIZED $isMaximized")
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val height = dragView.measuredHeight
        dragView.layout(dragViewLeft, dragViewTop, r + dragViewLeft, height + dragViewTop)
        if (isMaximized) {
            secondView?.layout(l, height + dragViewTop, r, b + dragViewTop)
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
        val isPlayerHit = isViewHit(dragView, x, y)
        val isSecondViewHit = secondView?.let { isViewHit(it, x, y) } ?: false
        if (isPlayerHit && isClick(event)) {
            dragView.dispatchTouchEvent(event)
            performClick()
            return true
        }
        viewDragHelper.processTouchEvent(event)
        return isPlayerHit || isSecondViewHit
    }

    override fun performClick(): Boolean {
        return if (isMaximized) {
            return super.performClick()
        } else {
            maximize()
            false
        }
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
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                clickStartX = event.x
                clickStartY = event.y
            }
            MotionEvent.ACTION_UP -> {
                clickEndX = event.x
                clickEndY = event.y
                clickDuration = event.eventTime - event.downTime
            }
        }
        return clickStartX == clickEndX && clickStartY == clickEndY && clickDuration >= 0 && clickDuration < 300
    }

    fun maximize() {
        isMaximized = true
        secondView?.requestLayout()
        animate(1f, 1f)
        callback?.onMaximize()
    }

    fun minimize() {
        isMaximized = false
        pivotY = if (isPortrait) {
            height + secondView!!.height - bottomMargin
        } else {
            height - bottomMargin
        }
        secondView?.layout(0, 0, 0, 0)
        dragViewTop = 0
        animate(minScaleX, minScaleY)
        val top = PropertyValuesHolder.ofInt("top", 0)
        val bot = PropertyValuesHolder.ofInt("bottom", dragView.height)
        ObjectAnimator.ofPropertyValuesHolder(dragView, top, bot).apply {
            duration = ANIMATION_DURATION
            start()
        }
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
        println("SAVE")
        return bundleOf("superState" to super.onSaveInstanceState(), "isMaximized" to isMaximized)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state.let {
            if (it is Bundle) {
                isMaximized = it.getBoolean("isMaximized")
                println("RESTORE $isMaximized")
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