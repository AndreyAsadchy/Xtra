package com.github.exact7.xtra.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

object DisplayUtils {
    fun convertDpToPixels(context: Context, dp: Float) =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()

    fun convertPixelsToDp(context: Context, pixels: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, context.resources.displayMetrics).toInt()

    fun getDisplayDensity(context: Context) = context.resources.displayMetrics.density

    fun calculateLandscapeWidthByPercent(percent: Int): Int {
        val deviceLandscapeWidth = with(Resources.getSystem().displayMetrics) {
            if (heightPixels > widthPixels) heightPixels else widthPixels
        }
        return (deviceLandscapeWidth * (percent / 100f)).toInt()
    }

    fun calculatePortraitHeightByPercent(percent: Int): Int {
        val devicePortraitHeight = with(Resources.getSystem().displayMetrics) {
            if (widthPixels > heightPixels) widthPixels else heightPixels
        }
        return (devicePortraitHeight * (percent / 100f)).toInt()
    }
}