package com.github.exact7.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.MarginItemDecoration
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.isInPortraitOrientation
import com.github.exact7.xtra.util.prefs

class GridRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        if (!isInEditMode) {
            val prefs = context.prefs()
            val count = if (context.isInPortraitOrientation) {
                prefs.getString(C.PORTRAIT_COLUMN_COUNT, "1")!!.toInt()
            } else {
                prefs.getString(C.LANDSCAPE_COLUMN_COUNT, "2")!!.toInt()
            }
            layoutManager = GridLayoutManager(context, count)
            addItemDecoration(if (count > 1) MarginItemDecoration(context.resources.getDimension(R.dimen.divider_margin).toInt(), count) else DividerItemDecoration(context, GridLayoutManager.VERTICAL))
        }
    }
}