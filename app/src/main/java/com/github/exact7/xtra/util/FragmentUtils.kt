package com.github.exact7.xtra.util

import android.content.Context
import android.content.res.Configuration
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.MarginItemDecorator
import com.github.exact7.xtra.ui.common.RadioButtonDialogFragment

object FragmentUtils {

    /**
     * Use this when result should be a string resource id
     */
    fun showRadioButtonDialogFragment(context: Context, fragmentManager: FragmentManager, labels: List<Int>, checkedIndex: Int) {
        RadioButtonDialogFragment.newInstance(
                labels.map(context::getString),
                labels.toIntArray(),
                checkedIndex
        ).show(fragmentManager, null)
    }

    /**
     * Use this when result should be an checkedIndex
     */
    fun showRadioButtonDialogFragment(fragmentManager: FragmentManager, labels: Collection<CharSequence>, checkedIndex: Int) {
        RadioButtonDialogFragment.newInstance(
                labels,
                null,
                checkedIndex
        ).show(fragmentManager, null)
    }

    fun setRecyclerViewSpanCount(recyclerView: RecyclerView) {
        if (recyclerView.layoutManager is GridLayoutManager) {
            val context = recyclerView.context
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            with(recyclerView.layoutManager as GridLayoutManager) {
                if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val count = prefs.getString(C.PORTRAIT_COLUMN_COUNT, "1")!!.toInt()
                    recyclerView.addItemDecoration(if (count > 1) MarginItemDecorator(R.dimen.divider_margin) else DividerItemDecoration(context, GridLayoutManager.VERTICAL))
                    spanCount = count
                } else {
                    val count = prefs.getString(C.LANDSCAPE_COLUMN_COUNT, "1")!!.toInt()
                    recyclerView.addItemDecoration(if (count > 1) MarginItemDecorator(R.dimen.divider_margin) else DividerItemDecoration(context, GridLayoutManager.VERTICAL))
                    spanCount = count
                }
            }
        }
    }
}