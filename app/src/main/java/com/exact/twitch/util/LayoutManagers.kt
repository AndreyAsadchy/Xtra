package com.exact.twitch.util

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

object LayoutManagers {

    @JvmOverloads
    @JvmStatic fun linear(context: Context, orientation: Int = androidx.recyclerview.widget.RecyclerView.VERTICAL, reverseLayout: Boolean = false): androidx.recyclerview.widget.LinearLayoutManager {
        return androidx.recyclerview.widget.LinearLayoutManager(context, orientation, reverseLayout)
    }

    @JvmOverloads
    @JvmStatic fun grid(context: Context, spanCount: Int = 1, orientation: Int = androidx.recyclerview.widget.RecyclerView.VERTICAL, reverseLayout: Boolean = false): androidx.recyclerview.widget.GridLayoutManager {
        return androidx.recyclerview.widget.GridLayoutManager(context, spanCount, orientation, reverseLayout)
    }

    @JvmOverloads
    @JvmStatic fun staggeredGrid(spanCount: Int = 1, orientation: Int = androidx.recyclerview.widget.RecyclerView.VERTICAL): androidx.recyclerview.widget.StaggeredGridLayoutManager {
        return androidx.recyclerview.widget.StaggeredGridLayoutManager(spanCount, orientation)
    }
}