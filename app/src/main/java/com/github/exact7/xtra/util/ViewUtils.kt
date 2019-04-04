package com.github.exact7.xtra.util

import android.content.Context
import android.util.TypedValue

object ViewUtils {
    fun convertDpToPixels(context: Context, dp: Float) =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

    fun convertPixelsToDp(context: Context, pixels: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, context.resources.displayMetrics).toInt()
}