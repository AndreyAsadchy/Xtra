package com.exact.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.ScaleAnimation
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import com.google.android.exoplayer2.ui.PlayerView

class SlidingView : RelativeLayout {

    private val viewDragHelper = ViewDragHelper.create(this, 1f, SlidingCallback())

    private lateinit var dragView: View
    private lateinit var secondView: View

    private var topBound: Int = 0
    private var bottomBound: Int = 0
    private var minimizedTop: Int = 0
    private var minimizedLeft: Int = 0
    private var minimizeThreshold: Int = 0

    private val marginRight = 100
    private val marginBottom = 400

    internal var startX = 0f
    internal var startY = 0f
    internal var endX = 0f
    internal var endY = 0f
    internal var duration: Long = 0

    private var controllerVisibility: Int = 0

    internal var left: Int = 0
    internal var top: Int = 0


    private val isViewAtBottom: Boolean
        get() = dragView.top == minimizedTop

    private val isViewAtRight: Boolean
        get() = dragView.left == minimizedLeft

    private val isViewMinimized: Boolean
        get() = isViewAtBottom && isViewAtRight

    private val isFullscreen: Boolean
        get() = dragView.left == 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeAttributes(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeAttributes(attrs)
    }

    private fun initializeAttributes(attrs: AttributeSet) {

    }


    override fun onFinishInflate() {
        super.onFinishInflate()
        val childCount = childCount
        if (childCount != 2) {
            throw IllegalArgumentException()
        }
        dragView = getChildAt(0)
        secondView = getChildAt(1)
        if (dragView is PlayerView) {
            (dragView as PlayerView).setControllerVisibilityListener { controllerVisibility = it }
        }
        dragView.post {
            topBound = paddingTop
            bottomBound = (dragView.height / 1.5).toInt()
            minimizedLeft = width - dragView.width - marginRight
            minimizedTop = 400
            minimizeThreshold = dragView.height / 3
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel()
            return false
        }
        return viewDragHelper.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //        if (!isViewMinimized() && dragView instanceof PlayerView && isClicked(event)) {
        //            performClick();
        //            return true;
        //        }
        //        viewDragHelper.processTouchEvent(event);
        //        if (isViewMinimized() && isClicked(event)) {
        //            maximize();
        //        }

        val animation = ScaleAnimation(1f, 0.1f, 1f, 0f)
        animation.start()
        return true
    }

    override fun performClick(): Boolean {
        val playerView = dragView as PlayerView?
        if (controllerVisibility == View.VISIBLE) {
            playerView!!.hideController()
        } else if (controllerVisibility == View.GONE) {
            playerView!!.showController()
        }
        return super.performClick()
    }

    private fun isClicked(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_UP -> {
                endX = event.x
                endY = event.y
                duration = event.eventTime - event.downTime
            }
        }
        return startX == endX && startY == endY && duration >= 0 && duration < 300
    }

    fun maximize() {
        secondView.visibility = View.VISIBLE
        smoothSlideTo(0f)
        val margingParams = ViewGroup.MarginLayoutParams(dragView.layoutParams)
        margingParams.bottomMargin = 0
        margingParams.rightMargin = 0
        val params = RelativeLayout.LayoutParams(margingParams)
        dragView.layoutParams = params
    }

    fun minimize() {
        secondView.visibility = View.GONE
        smoothSlideTo(1f)
        val margingParams = ViewGroup.MarginLayoutParams(dragView.layoutParams)
        margingParams.bottomMargin = marginBottom
        margingParams.rightMargin = marginRight
        val params = RelativeLayout.LayoutParams(margingParams)
        dragView.layoutParams = params
    }

    private fun smoothSlideTo(slideOffset: Float): Boolean {
        val left = (slideOffset * minimizedLeft).toInt()
        val top = (slideOffset * minimizedTop).toInt()
        if (viewDragHelper!!.smoothSlideViewTo(dragView, left, top)) {
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    private fun closeToRight() {
        if (viewDragHelper!!.smoothSlideViewTo(dragView, dragView.left + dragView.width, dragView.top)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun closeToLeft() {
        if (viewDragHelper!!.smoothSlideViewTo(dragView, -dragView.width, dragView.top)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun computeScroll() {
        if (viewDragHelper!!.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isInEditMode) {
            super.onLayout(changed, l, t, r, b)
        } else {
            dragView.layout(l, t, r, dragView.height)
            dragView.y = top.toFloat()
            if (!isViewMinimized) {
                secondView.layout(l, dragView.height, r, b)
                secondView.y = dragView.height.toFloat()

            }
        }
    }

    private inner class SlidingCallback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return child === dragView
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if (!isFullscreen) child.top else Math.min(Math.max(top, topBound), bottomBound) //new top
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return if (isViewAtBottom && child.left != 0) left else child.left //new left
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            this@SlidingView.top = top
            this@SlidingView.left = left
            if (!isFullscreen && !isViewMinimized) {
                val dragOffset = top.toFloat() / changedView.height
                dragView.scaleX = 1 - dragOffset / 2
                dragView.scaleY = 1 - dragOffset / 2
                secondView.translationY = top.toFloat()
            } else if (isFullscreen) {
                secondView.translationY = top.toFloat()
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            if (isFullscreen) {
                if (releasedChild.top >= minimizeThreshold) {
                    minimize()
                    secondView.layout(0, 0, 0, 0)
                } else {
                    releasedChild.top = 0
                    secondView.translationY = 0f
                }
            } else {
                if (xvel > 1500) {
                    closeToRight()
                } else if (xvel < -1500) {
                    closeToLeft()
                } else {
                    if (!isViewMinimized) {
                        minimize()
                    }
                }
            }
        }
    }
}
