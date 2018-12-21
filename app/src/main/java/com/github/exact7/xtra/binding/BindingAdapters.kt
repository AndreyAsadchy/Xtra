package com.github.exact7.xtra.binding

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.github.exact7.xtra.GlideApp
import com.github.exact7.xtra.R
import java.util.Calendar
import kotlin.math.roundToInt

@SuppressLint("CheckResult")
@BindingAdapter("imageUrl", "changes", "circle", requireAll = false)
fun loadImage(imageView: ImageView, url: String, changes: Boolean, circle: Boolean) {
    val request = GlideApp.with(imageView.context)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
    if (changes) {
        val calendar = Calendar.getInstance()
        request.signature(ObjectKey((calendar.get(Calendar.MINUTE) / 10f).roundToInt() + calendar.get(Calendar.HOUR) + calendar.get(Calendar.DAY_OF_MONTH)))
    }
    if (circle) {
        request.circleCrop()
    }
    request.into(imageView)
}

@BindingAdapter("divider")
fun setDivider(recyclerView: RecyclerView, divider: Drawable) {
    val context = recyclerView.context
    recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.dividerColor, typedValue, true)
        divider.setColorFilter(typedValue.data, PorterDuff.Mode.SRC)
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

//@BindingAdapter("enabled")
//fun setEnabled(imageView: ImageView, enabled: Boolean) {
//    imageView.apply {
//        isEnabled = enabled
//        setColorFilter(if (enabled) Color.WHITE else Color.GRAY)
//    }
//}