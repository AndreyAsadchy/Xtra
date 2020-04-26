package com.github.exact7.xtra.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.exact7.xtra.ui.isClick
import com.google.android.exoplayer2.ui.PlayerView

class CustomPlayerView : PlayerView {

    private val clickCallback = Runnable {
        performClick()
    }
    private var downTouchLocation = FloatArray(2)
    private var lastClickTime = 0L

    private lateinit var listener: () -> Unit

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.isClick(downTouchLocation)) {
            val currentTime = System.currentTimeMillis()
            postDelayed(clickCallback, DOUBLE_TAP_THRESHOLD)
            if (currentTime - lastClickTime < DOUBLE_TAP_THRESHOLD) {
                removeCallbacks(clickCallback)
                listener()
            }
            lastClickTime = currentTime
        }
        return true
    }

    fun setOnDoubleTapListener(action: () -> Unit) {
        listener = action
    }

    companion object {
        private const val DOUBLE_TAP_THRESHOLD = 300L
    }
}