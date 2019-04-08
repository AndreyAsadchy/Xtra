package com.github.exact7.xtra.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.util.TwitchApiHelper
import java.util.Calendar

@SuppressLint("CheckResult")
@BindingAdapter("imageUrl", "changes", "circle", requireAll = false)
fun loadImage(imageView: ImageView, url: String?, changes: Boolean, circle: Boolean) {
    val activity = imageView.context as Activity
    if (activity.isFinishing) return
    val request = GlideApp.with(activity)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
    if (changes) {
        val calendar = Calendar.getInstance()
        request.signature(ObjectKey(Math.floor(calendar.get(Calendar.MINUTE) / 10.0 * 2.0) / 2.0 + calendar.get(Calendar.HOUR) + calendar.get(Calendar.DAY_OF_MONTH)))
    }
    if (circle) {
        request.circleCrop()
    }
    request.into(imageView)
}

@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean?) {
    view.visibility = if (visible == true) View.VISIBLE else View.GONE
}

@BindingAdapter("enabled")
fun setEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
}

@BindingAdapter("enabled")
fun setEnabled(imageView: ImageView, enabled: Boolean) {
    imageView.apply {
        isEnabled = enabled
        setColorFilter(if (enabled) Color.WHITE else Color.GRAY)
    }
}

@BindingAdapter("tint")
fun setTint(imageView: ImageView, color: Int) {
    val drawable = imageView.drawable
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        drawable.setTint(color)
    } else {
        val wrap = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(wrap, color)
    }
}

@BindingAdapter("date")
fun setDate(textView: TextView, date: Long) {
    textView.text = TwitchApiHelper.formatTime(textView.context, date)
}

@BindingAdapter("date")
fun setDate(textView: TextView, date: String) {
    textView.text = TwitchApiHelper.formatTime(textView.context, TwitchApiHelper.parseIso8601Date(date))
}

@BindingAdapter("duration")
fun setDuration(textView: TextView, duration: Long) {
    textView.text = DateUtils.formatElapsedTime(duration)
}