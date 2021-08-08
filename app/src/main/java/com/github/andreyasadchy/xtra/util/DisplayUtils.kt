package com.github.andreyasadchy.xtra.util

import android.content.Context

object DisplayUtils {
    fun calculateLandscapeWidthByPercent(context: Context, percent: Int): Int {
        val deviceLandscapeWidth = with(context.resources.displayMetrics) {
            if (heightPixels > widthPixels) heightPixels else widthPixels
        }
        return (deviceLandscapeWidth * (percent / 100f)).toInt()
    }

    fun calculatePortraitHeightByPercent(context: Context, percent: Int): Int {
        val devicePortraitHeight = with(context.resources.displayMetrics) {
            if (widthPixels > heightPixels) widthPixels else heightPixels
        }
        return (devicePortraitHeight * (percent / 100f)).toInt()
    }
}