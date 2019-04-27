package com.github.exact7.xtra.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.MarginItemDecoration
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
     * Use this when result should be an index
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
            val prefs = context.prefs()
            val count = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                prefs.getString(C.PORTRAIT_COLUMN_COUNT, "1")!!.toInt()
            } else {
                prefs.getString(C.LANDSCAPE_COLUMN_COUNT, "2")!!.toInt()
            }
            (recyclerView.layoutManager as GridLayoutManager).spanCount = count
            recyclerView.addItemDecoration(if (count > 1) MarginItemDecoration(context.resources.getDimension(R.dimen.divider_margin).toInt(), count) else DividerItemDecoration(context, GridLayoutManager.VERTICAL))
        }
    }

    fun showUnfollowDialog(activity: Activity, channelName: String, positiveCallback: () -> Unit) {
        AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.unfollow, channelName))
                .setPositiveButton(R.string.yes) { _, _ -> positiveCallback.invoke()}
                .setNegativeButton(R.string.no) { _, _ -> }
                .show()
    }
}