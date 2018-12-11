package com.github.exact7.xtra.binding

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.github.exact7.xtra.GlideApp

@SuppressLint("CheckResult")
@BindingAdapter("imageUrl", "signature", "circle", requireAll = false)
fun loadImage(imageView: ImageView, url: String, signature: Any?, circle: Boolean) {
    println(signature)
    val request = GlideApp.with(imageView.context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
    signature?.let { request.signature(ObjectKey(it)) }
    if (circle) {
        request.circleCrop()
    }
    request.into(imageView)
}

@BindingAdapter("divider")
fun setDivider(recyclerView: RecyclerView, divider: Drawable) {
    val context = recyclerView.context
    recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
        setDrawable(divider)
    })
}

@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean?) {
    view.visibility = if (visible == true) View.VISIBLE else View.GONE
}

@BindingAdapter("enabled")
fun setEnabled(view: View, enabled: Boolean) {
    view.isEnabled = enabled
}

//@BindingAdapter("enabled")
//fun setEnabled(imageView: ImageView, enabled: Boolean) {
//    imageView.apply {
//        isEnabled = enabled
//        setColorFilter(if (enabled) Color.WHITE else Color.GRAY)
//    }
//}