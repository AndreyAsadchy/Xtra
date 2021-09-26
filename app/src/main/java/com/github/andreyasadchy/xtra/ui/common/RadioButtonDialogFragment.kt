package com.github.andreyasadchy.xtra.ui.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.os.bundleOf


class RadioButtonDialogFragment : ExpandingBottomSheetDialogFragment() {

    interface OnSortOptionChanged {
        fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?)
    }

    companion object {

        private const val REQUEST_CODE = "requestCode"
        private const val LABELS = "labels"
        private const val TAGS = "tags"
        private const val CHECKED = "checked"

        fun newInstance(requestCode: Int, labels: Collection<CharSequence>, tags: IntArray? = null, checkedIndex: Int): RadioButtonDialogFragment {
            return RadioButtonDialogFragment().apply {
                arguments = bundleOf(REQUEST_CODE to requestCode, LABELS to ArrayList(labels), TAGS to tags, CHECKED to checkedIndex)
            }
        }
    }

    private lateinit var listenerSort: OnSortOptionChanged

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listenerSort = parentFragment as OnSortOptionChanged
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val context = requireContext()
        val arguments = requireArguments()
        val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        val radioGroup = RadioGroup(context).also { it.layoutParams = layoutParams }
        val checkedId = arguments.getInt(CHECKED)
        val clickListener = View.OnClickListener { v ->
            val clickedId = v.id
            if (clickedId != checkedId) {
                listenerSort.onChange(arguments.getInt(REQUEST_CODE), clickedId, (v as RadioButton).text, v.tag as Int?)
            }
            dismiss()
        }
        val tags = arguments.getIntArray(TAGS)
        arguments.getCharSequenceArrayList(LABELS)?.forEachIndexed { index, label ->
            val button = AppCompatRadioButton(context).apply {
                id = index
                text = label
                tag = tags?.getOrNull(index)
                setOnClickListener(clickListener)
            }
            radioGroup.addView(button, layoutParams)
        }
        radioGroup.check(checkedId)
        return radioGroup
    }
}