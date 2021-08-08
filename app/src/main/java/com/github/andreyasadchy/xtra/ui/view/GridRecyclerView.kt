package com.github.andreyasadchy.xtra.ui.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.common.MarginItemDecoration
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.isInPortraitOrientation
import com.github.andreyasadchy.xtra.util.prefs

class GridRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val prefs = context.prefs()
    private val portraitColumns = prefs.getString(C.PORTRAIT_COLUMN_COUNT, "1")!!.toInt()
    private val landscapeColumns = prefs.getString(C.LANDSCAPE_COLUMN_COUNT, "2")!!.toInt()

    private val gridLayoutManager: GridLayoutManager

    init {
        val columns = getColumnsForConfiguration(resources.configuration)
        gridLayoutManager = GridLayoutManager(context, columns)
        layoutManager = gridLayoutManager
        addItemDecoration(columns)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        removeItemDecorationAt(0)
        val columns = getColumnsForConfiguration(newConfig)
        gridLayoutManager.spanCount = columns
        addItemDecoration(columns)
    }

    private fun getColumnsForConfiguration(configuration: Configuration): Int {
        return if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) portraitColumns else landscapeColumns
    }

    private fun addItemDecoration(columns: Int) {
        addItemDecoration(if (columns <= 1) DividerItemDecoration(context, GridLayoutManager.VERTICAL) else MarginItemDecoration(context.resources.getDimension(R.dimen.divider_margin).toInt(), columns))
    }
}