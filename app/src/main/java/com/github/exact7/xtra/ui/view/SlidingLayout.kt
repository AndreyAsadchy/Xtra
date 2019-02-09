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
import com.google.android.exoplayer2.ui.PlayerView

class SlidingLayout : RelativeLayout {

    private val viewDragHelper = ViewDragHelper.create(this, 1f, SlidingCallback())

    private lateinit var playerView: PlayerView
    private var secondView: View? = null

    private var topBound = 0
    private var bottomBound = 0
    private var minimizeThreshold = 0
    private var isPortrait = true

    private var playerViewTop = 0
    private var minScaleX = 0f
    private var minScaleY = 0f
    private val minPivotYPortrait: Float
        get() = (height).toFloat()
    private val minPivotYLandscape: Float
        get() = (height).toFloat()
    private var originalPivotX = 0f
    private var originalPivotY = 0f

    private var clickStartX = 0f
    private var clickStartY = 0f
    private var clickEndX = 0f
    private var clickEndY = 0f
    private var clickDuration = 0L

    private var isMaximized = true
    private var isAnimating = false

    private var callback: Callback? = null
    private val animationListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {}
        override fun onAnimationCancel(animation: Animator?) {}
        override fun onAnimationStart(animation: Animator?) { isAnimating = true }
        override fun onAnimationEnd(animation: Animator?) { isAnimating = false }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        playerView = getChildAt(0) as PlayerView
        secondView = getChildAt(1)
        playerView.post {
            topBound = paddingTop
            minimizeThreshold = height / 5
            originalPivotX = pivotX
            originalPivotY = pivotY
            if (playerView.height == height) {//landscape
                bottomBound = (height / 1.5f).toInt()
                minScaleX = 0.5f
                minScaleY = 0.5f
                isPortrait = false
            } else  { //portrait
                bottomBound = height / 2
                minScaleX = 0.5f
                minScaleY = 0.5f
            }
//            pivotX = width * 0.9f
            println("MAXIMIZED $isMaximized")
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            playerView.layout(l, playerViewTop, r, playerViewTop + playerView.measuredHeight)
            secondView?.layout(l, playerViewTop + playerView.measuredHeight, r, b + playerViewTop)
        if (isMaximized) {
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
        val interceptTap = viewDragHelper.isViewUnder(playerView, ev.x.toInt(), ev.y.toInt())
        return (viewDragHelper.shouldInterceptTouchEvent(ev) || interceptTap)
    }

//    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        return isViewHit(playerView, event.x.toInt(), event.y.toInt()).also { println(it) }
//    }

//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        if (isViewHit(playerView, event.x.toInt(), event.y.toInt())) {
//            viewDragHelper.processTouchEvent(event)
//            return true
//        } else {
//            return super.onTouchEvent(event)
//        }
//    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        viewDragHelper.processTouchEvent(event)
        val x = event.x.toInt()
        val y = event.y.toInt()
        val isPlayerHit = isViewHit(playerView, x, y)
        println("player $isPlayerHit")
        val isSecondViewHit = secondView?.let { isViewHit(it, x, y) } ?: false
        if (isPlayerHit && isClick(event)) {
            performClick()
            return true
        }
        return isPlayerHit || isSecondViewHit
    }

    override fun performClick(): Boolean {
        println("CLICK")
        return if (isMaximized) {
            return super.performClick()
        } else {
//            updateLayoutParams { (this as FrameLayout.LayoutParams).bottomMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt() }
//            postDelayed({
                pivotY = height.toFloat() + (secondView!!.height) - (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)) * 2
//            }, 500)
//            maximize()
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
        animate(1f, 1f, originalPivotY)
        playerViewTop = 0
        requestLayout()
        callback?.onMaximize()
    }

    fun minimize() {
        isMaximized = false
        secondView?.layout(0, 0, 0, 0)
        playerViewTop = 0
        requestLayout()
        animate(minScaleX, minScaleY, if (isPortrait) minPivotYPortrait else minPivotYLandscape)
        callback?.onMinimize()
    }

    private fun animate(scaleX: Float, scaleY: Float, pivotY: Float) {
        val sclX = PropertyValuesHolder.ofFloat("scaleX", scaleX)
        val sclY = PropertyValuesHolder.ofFloat("scaleY", scaleY)
//        val pvtY = PropertyValuesHolder.ofFloat("pivotY", pivotY)
        ObjectAnimator.ofPropertyValuesHolder(this, sclX, sclY).apply {
            duration = 300L
            addListener(animationListener)
            start()
        }

//        smoothSlideBack()
    }

    private fun smoothSlideBack(): Boolean {
        if (viewDragHelper.smoothSlideViewTo(playerView, 0, 0)) {
            postInvalidateOnAnimation()
            return true
        }
        return false
    }

    private fun closeTo(left: Int) {
        if (viewDragHelper.smoothSlideViewTo(playerView, left, playerView.top)) {
            postInvalidateOnAnimation()
        }
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
            return child == playerView
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if (isMaximized) Math.min(Math.max(top, topBound), bottomBound) else child.top
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (!isMaximized) left else child.left
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            playerViewTop = top
            secondView?.requestLayout()
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (isMaximized) {
                when {
                    releasedChild.top >= minimizeThreshold -> minimize()
                    else -> smoothSlideBack()
                }
            } else {
                when {
                    xvel > 1500 -> closeTo(width)
                    xvel < -1500 -> closeTo(-playerView.width)
                    else -> smoothSlideBack()
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