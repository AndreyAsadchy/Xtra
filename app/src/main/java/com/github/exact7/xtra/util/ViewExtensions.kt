package com.github.exact7.xtra.util

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.github.exact7.xtra.GlideApp
import java.util.Calendar

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible(value: Boolean) {
    visibility = if (value) View.VISIBLE else View.GONE
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

fun View.toggleVisibility() = if (isVisible()) gone() else visible()

@SuppressLint("CheckResult")
fun ImageView.loadImage(url: String?, changes: Boolean = false, circle: Boolean = false, diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.AUTOMATIC) {
    val context = context ?: return
    if (context is Activity && !context.window.decorView.isShown) {
        return
    }
    val request = GlideApp.with(context)
            .load(url)
            .diskCacheStrategy(diskCacheStrategy)
            .transition(DrawableTransitionOptions.withCrossFade())
    if (changes) {
        val calendar = Calendar.getInstance()
        request.signature(ObjectKey(Math.floor(calendar.get(Calendar.MINUTE) / 10.0 * 2.0) / 2.0 + calendar.get(Calendar.HOUR) + calendar.get(Calendar.DAY_OF_MONTH)))
    }
    if (circle) {
        request.circleCrop()
    }
    request.into(this)
}