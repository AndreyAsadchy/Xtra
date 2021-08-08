package com.github.andreyasadchy.xtra.ui.common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val sizeDp: Int, private val columnCount: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            val position = parent.getChildAdapterPosition(view)
            if (position >= columnCount) {
                top = sizeDp
            }
            if ((position + 1) % columnCount != 0) {
                right = sizeDp
            }
            bottom = sizeDp
        }
    }
}