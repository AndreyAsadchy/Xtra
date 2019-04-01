package com.github.exact7.xtra.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.exact7.xtra.R

object Utils {
    fun getNavigationIcon(context: Context): Drawable {
        val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_black_24)!!)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.textColor, typedValue, true)
        DrawableCompat.setTint(drawable, typedValue.data)
        return drawable
    }
}