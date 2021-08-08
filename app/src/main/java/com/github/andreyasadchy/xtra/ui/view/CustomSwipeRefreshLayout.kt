package com.github.andreyasadchy.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.github.andreyasadchy.xtra.R

class CustomSwipeRefreshLayout : SwipeRefreshLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        val theme = context.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(R.attr.primaryColor, typedValue, true)
        setProgressBackgroundColorSchemeColor(typedValue.data)
        theme.resolveAttribute(R.attr.textColor, typedValue, true)
        setColorSchemeColors(typedValue.data)
    }
}