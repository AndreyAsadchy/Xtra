package com.exact.twitch.ui.videos

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import com.exact.twitch.R
import kotlinx.android.synthetic.main.dialog_videos_sort.*

class VideosSortDialog : com.google.android.material.bottomsheet.BottomSheetDialogFragment() {

    interface OnFilterApplied {
        fun onApply(sortId: Int, sortText: CharSequence, periodId: Int, periodText: CharSequence)
    }

    companion object {

        private const val SORT = "sort"
        private const val PERIOD = "period"

        fun newInstance(sortId: Int, periodId: Int): VideosSortDialog {
            return VideosSortDialog().also {
                val args = Bundle(2)
                args.putInt(SORT, sortId)
                args.putInt(PERIOD, periodId)
                it.arguments = args
            }
        }
    }

    private var listener: OnFilterApplied? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (parentFragment is OnFilterApplied) {
            listener = parentFragment as OnFilterApplied
        } else {
            throw RuntimeException(parentFragment.toString() + " must implement OnFilterApplied")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_videos_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sort.check(arguments?.getInt(SORT)!!)
        period.check(arguments?.getInt(PERIOD)!!)
        apply.setOnClickListener {
            val sortBtn = view.findViewById<RadioButton>(sort.checkedRadioButtonId)
            val periodBtn = view.findViewById<RadioButton>(period.checkedRadioButtonId)
            listener!!.onApply(sortBtn.id, sortBtn.text, periodBtn.id, periodBtn.text)
            dismiss()
        }
    }
}
