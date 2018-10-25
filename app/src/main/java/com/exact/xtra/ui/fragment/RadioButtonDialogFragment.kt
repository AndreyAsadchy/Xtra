package com.exact.xtra.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.os.bundleOf
import com.exact.xtra.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RadioButtonDialogFragment : BottomSheetDialogFragment() {

    interface OnSortOptionChanged {
        fun onChange(index: Int, text: CharSequence, tag: Int?)
    }

    companion object {

        private const val LABELS = "labels"
        private const val TAGS = "tags"
        private const val CHECKED = "checked"

        fun newInstance(labels: List<CharSequence>, tags: IntArray? = null, checkedIndex: Int): RadioButtonDialogFragment {
            return RadioButtonDialogFragment().apply {
                arguments = bundleOf(LABELS to ArrayList(labels), TAGS to tags, CHECKED to checkedIndex)
            }
        }
    }

    private var listenerSort: OnSortOptionChanged? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnSortOptionChanged) {
            listenerSort = parentFragment as OnSortOptionChanged
        } else {
            throw RuntimeException(parentFragment.toString() + " must implement RadioButtonDialogFragment.OnSortOptionChanged")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val radioGroup = inflater.inflate(R.layout.dialog_radio_button, container, false) as RadioGroup
        arguments?.let {
            val checkedId = it.getInt(CHECKED)
            val clickListener = View.OnClickListener { v ->
                val clickedId = v.id
                if (clickedId != checkedId) {
                    listenerSort?.onChange(clickedId, (v as RadioButton).text, v.tag as Int?)
                }
                dismiss()
            }
            val tags = it.getIntArray(TAGS)
            it.getCharSequenceArrayList(LABELS)?.forEachIndexed { index, label ->
                val button = RadioButton(requireActivity()).apply {
                    id = index
                    text = label
                    tag = tags?.getOrNull(index)
                    setOnClickListener(clickListener)
                }
                radioGroup.addView(button, MATCH_PARENT, WRAP_CONTENT)
            }
            radioGroup.check(checkedId)
        }
        return radioGroup
    }

    override fun onDetach() {
        super.onDetach()
        listenerSort = null
    }
}
