package com.github.exact7.xtra.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecorator(private val sizeDp: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            val position = parent.getChildAdapterPosition(view)
            if (position > 0) {
                top = sizeDp
            }
            if (position % 2 == 0) {
                right = sizeDp
            }
            bottom = sizeDp
        }
    }
}