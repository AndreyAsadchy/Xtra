package com.github.exact7.xtra.ui.videos.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.videos.Period
import com.github.exact7.xtra.ui.videos.Period.ALL
import com.github.exact7.xtra.ui.videos.Period.DAY
import com.github.exact7.xtra.ui.videos.Period.MONTH
import com.github.exact7.xtra.ui.videos.Period.WEEK
import com.github.exact7.xtra.ui.videos.Sort
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_videos_sort.*

class GameVideosSortDialog : BottomSheetDialogFragment() {

    interface OnFilterApplied {
        fun onApply(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence)
    }

    companion object {

        private const val SORT = "sort"
        private const val PERIOD = "period"

        fun newInstance(sort: Sort, period: Period): GameVideosSortDialog {
            return GameVideosSortDialog().apply {
                arguments = bundleOf(SORT to sort, PERIOD to period)
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
        sort.check(if (arguments?.getSerializable(SORT) as Sort == Sort.TIME) R.id.time else R.id.views)
        period.check(when (arguments?.getSerializable(PERIOD) as Period) {
            DAY -> R.id.today
            WEEK -> R.id.week
            MONTH -> R.id.month
            ALL -> R.id.all
        })
        apply.setOnClickListener {
            val sortBtn = view.findViewById<RadioButton>(sort.checkedRadioButtonId)
            val periodBtn = view.findViewById<RadioButton>(period.checkedRadioButtonId)
            listener!!.onApply(
                    if (sortBtn.id == R.id.time) Sort.TIME else Sort.VIEWS,
                    sortBtn.text,
                    when (periodBtn.id) {
                        R.id.today -> DAY
                        R.id.week -> WEEK
                        R.id.month -> MONTH
                        R.id.all -> ALL
                        else -> throw IllegalStateException()
                    },
                    periodBtn.text)
            dismiss()
        }
    }
}
