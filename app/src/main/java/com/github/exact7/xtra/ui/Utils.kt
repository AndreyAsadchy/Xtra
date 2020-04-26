package com.github.exact7.xtra.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.github.exact7.xtra.R

internal object Utils {
    fun getNavigationIcon(context: Context): Drawable { //TODO change this to accept toolbar as parameter
        val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(context, R.drawable.baseline_arrow_back_black_24)!!)
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.textColor, typedValue, true)
        DrawableCompat.setTint(drawable, typedValue.data)
        DrawableCompat.setAutoMirrored(drawable, true)
        return drawable
    }
}

fun MotionEvent.isClick(outDownLocation: FloatArray): Boolean {
    return when (actionMasked) {
        MotionEvent.ACTION_DOWN -> {
            outDownLocation[0] = x
            outDownLocation[1] = y
            false
        }
        MotionEvent.ACTION_UP -> {
            outDownLocation[0] in x - 50..x + 50 && outDownLocation[1] in y - 50..y + 50 && eventTime - downTime <= 500
        }
        else -> false
    }
}