package com.github.exact7.xtra.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.exact7.xtra.util.isClick
import com.google.android.exoplayer2.ui.PlayerView

class CustomPlayerView : PlayerView {

    private var downTouchLocation = FloatArray(2)

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.isClick(downTouchLocation)) {
            performClick()
        }
        return true
    }
}