package com.github.exact7.xtra.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.exoplayer2.ui.PlayerView

class CustomPlayerView : PlayerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return (ev.actionMasked == MotionEvent.ACTION_UP).also {
            if (isControllerVisible) {
                hideController()
            } else {
                showController()
            }
        }
    }
}