package com.github.andreyasadchy.xtra.ui.videos.game

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.video.Period
import com.github.andreyasadchy.xtra.model.kraken.video.Period.ALL
import com.github.andreyasadchy.xtra.model.kraken.video.Period.DAY
import com.github.andreyasadchy.xtra.model.kraken.video.Period.MONTH
import com.github.andreyasadchy.xtra.model.kraken.video.Period.WEEK
import com.github.andreyasadchy.xtra.model.kraken.video.Sort
import com.github.andreyasadchy.xtra.model.kraken.video.Sort.TIME
import com.github.andreyasadchy.xtra.model.kraken.video.Sort.VIEWS
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_videos_sort.*

class GameVideosSortDialog : ExpandingBottomSheetDialogFragment() {

    interface OnFilter {
        fun onChange(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence)
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

    private lateinit var listener: OnFilter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnFilter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_videos_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val originalSortId = if (args.getSerializable(SORT) as Sort == TIME) R.id.time else R.id.views
        val originalPeriodId = when (args.getSerializable(PERIOD) as Period) {
            DAY -> R.id.today
            WEEK -> R.id.week
            MONTH -> R.id.month
            ALL -> R.id.all
        }
        sort.check(originalSortId)
        period.check(originalPeriodId)
        apply.setOnClickListener {
            val checkedPeriodId = period.checkedRadioButtonId
            val checkedSortId = sort.checkedRadioButtonId
            if (checkedPeriodId != originalPeriodId || checkedSortId != originalSortId) {
                val sortBtn = view.findViewById<RadioButton>(checkedSortId)
                val periodBtn = view.findViewById<RadioButton>(checkedPeriodId)
                listener.onChange(
                        if (checkedSortId == R.id.time) TIME else VIEWS,
                        sortBtn.text,
                        when (checkedPeriodId) {
                            R.id.today -> DAY
                            R.id.week -> WEEK
                            R.id.month -> MONTH
                            else -> ALL
                        },
                        periodBtn.text)
            }
            dismiss()
        }
    }
}
