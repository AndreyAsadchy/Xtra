package com.github.exact7.xtra.util

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue

object DisplayUtils {
    fun convertDpToPixels(dp: Float) =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics).toInt()

    fun convertPixelsToDp(pixels: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, displayMetrics).toInt()

    fun getDisplayDensity() = displayMetrics.density

    fun calculateLandscapeWidthByPercent(percent: Int): Int {
        val deviceLandscapeWidth = with(displayMetrics) {
            if (heightPixels > widthPixels) heightPixels else widthPixels
        }
        return (deviceLandscapeWidth * (percent / 100f)).toInt()
    }

    fun calculatePortraitHeightByPercent(percent: Int): Int {
        val devicePortraitHeight = with(displayMetrics) {
            if (widthPixels > heightPixels) widthPixels else heightPixels
        }
        return (devicePortraitHeight * (percent / 100f)).toInt()
    }

    private val displayMetrics: DisplayMetrics
        get() = Resources.getSystem().displayMetrics
}