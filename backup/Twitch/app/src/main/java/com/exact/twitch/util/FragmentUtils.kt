package com.exact.twitch.util

import android.content.Context
import android.graphics.Color
import android.support.v4.app.FragmentManager
import android.widget.RadioButton

import com.exact.twitch.ui.fragment.RadioButtonBottomSheetDialogFragment

import java.util.ArrayList
import java.util.Arrays

object FragmentUtils {

    @JvmOverloads
    fun showRadioButtonDialogFragment(context: Context, fragmentManager: FragmentManager, labels: IntArray, selectedItem: Int, useLabelsAsId: Boolean = true) {
        val list = ArrayList<RadioButton>(labels.size)
        for (i in labels.indices) {
            val radioButton = RadioButton(context) //TODO add style
            val label = labels[i]
            radioButton.id = if (useLabelsAsId) label else i
            radioButton.text = context.getString(label)
            styleRadioButton(radioButton)
            list.add(radioButton)
        }
        RadioButtonBottomSheetDialogFragment.newInstance(list, selectedItem).show(fragmentManager, null)
    }

    fun showRadioButtonDialogFragment(context: Context, fragmentManager: FragmentManager, labels: List<CharSequence>, selectedItem: Int) {
        val list = ArrayList<RadioButton>(labels.size)
        for (i in labels.indices) {
            val radioButton = RadioButton(context) //TODO add style
            radioButton.id = i
            radioButton.text = labels[i]
            styleRadioButton(radioButton)
            list.add(radioButton)
        }
        RadioButtonBottomSheetDialogFragment.newInstance(list, selectedItem).show(fragmentManager, null)
    }

    private fun styleRadioButton(radioButton: RadioButton) {
        radioButton.setTextColor(Color.BLACK)
    }
}