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
import com.exact.xtra.R

class RadioButtonDialogFragment : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    interface OnOptionSelectedListener {
        fun onSelect(index: Int, text: CharSequence, tag: Int?)
    }

    companion object {

        private const val LABELS = "labels"
        private const val TAGS = "tags"
        private const val CHECKED = "checked"

        fun newInstance(labels: List<CharSequence>, tags: IntArray? = null, checkedIndex: Int): RadioButtonDialogFragment {
            return RadioButtonDialogFragment().also {
                val args = Bundle()
                args.putCharSequenceArrayList(LABELS, ArrayList(labels))
                args.putIntArray(TAGS, tags)
                args.putInt(CHECKED, checkedIndex)
                it.arguments = args
            }
        }
    }

    private var listener: OnOptionSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnOptionSelectedListener) {
            listener = parentFragment as OnOptionSelectedListener
        } else {
            throw RuntimeException(parentFragment.toString() + " must implement RadioButtonDialogFragment.OnOptionSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val radioGroup = inflater.inflate(R.layout.dialog_radio_button, container, false) as RadioGroup
        arguments?.let {
            val clickListener = View.OnClickListener { v ->
                listener?.onSelect(v.id, (v as RadioButton).text, v.tag as Int?)
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
            radioGroup.check(it.getInt(CHECKED))
        }
        return radioGroup
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
