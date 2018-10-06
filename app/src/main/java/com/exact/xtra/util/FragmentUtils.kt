package com.exact.xtra.util

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.exact.xtra.ui.fragment.RadioButtonDialogFragment

object FragmentUtils {

    /**
     * Use this when result should be a string resource id
     */
    fun showRadioButtonDialogFragment(context: Context, fragmentManager: androidx.fragment.app.FragmentManager, labels: List<Int>, defaultIndex: Int, tag: String) {
       show(context,fragmentManager, ArrayList(labels.map(context::getString)), labels.toIntArray(), defaultIndex, tag)
    }

    /**
     * Use this when result should be an index
     */
    fun showRadioButtonDialogFragment(context: Context, fragmentManager: androidx.fragment.app.FragmentManager, labels: List<CharSequence>, tag: String) {
       show(context = context, fragmentManager = fragmentManager, labels = labels, tag = tag)
    }

    private fun show(context: Context, fragmentManager: androidx.fragment.app.FragmentManager, labels: List<CharSequence>, tags: IntArray? = null, defaultIndex: Int = 0, tag: String) {
        RadioButtonDialogFragment.newInstance(
                labels,
                tags,
                context.getSharedPreferences(C.USER_PREFS, MODE_PRIVATE).getInt(tag, defaultIndex)
        ).show(fragmentManager, null)
    }
}