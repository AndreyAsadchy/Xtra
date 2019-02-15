package com.github.exact7.xtra.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecorator(private val sizeDp: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) > 0) {
                top = sizeDp
            }
            left =  sizeDp
            right = sizeDp
            bottom = sizeDp
        }
    }
}