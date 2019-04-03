package com.github.exact7.xtra.util

import android.content.Context
import android.util.TypedValue

object ViewUtils {
    fun convertDpToPixels(context: Context, dp: Float) = try { //TODO
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt().also { println("VAL $it") }
    } catch (e: NullPointerException) {
        println("NULL $context")
        0
    }.also { println("RETURN $it") }

    fun convertPixelsToDp(context: Context, pixels: Float) = try {
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, context.resources.displayMetrics).toInt()
    } catch (e: NullPointerException) {
        println("NULL $context")
        0
    }
}