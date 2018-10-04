package com.exact.twitch.binding

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.exact.twitch.GlideApp

@SuppressLint("CheckResult")
@BindingAdapter("imageUrl", "fit", "circle", requireAll = false)
fun loadImage(imageView: ImageView, url: String?, fit: Boolean, circle: Boolean) {
    val request = GlideApp.with(imageView.context)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(DrawableTransitionOptions.withCrossFade())
    if (fit) {
        request.centerCrop()
    }
    if (circle) {
        request.circleCrop()
    }
    request.into(imageView)
}

@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("enabled")
fun setEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
}