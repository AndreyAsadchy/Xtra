package com.github.exact7.xtra.ui.follow.channels

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.os.bundleOf
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.follows.Order
import com.github.exact7.xtra.model.kraken.follows.Order.ASC
import com.github.exact7.xtra.model.kraken.follows.Order.DESC
import com.github.exact7.xtra.model.kraken.follows.Sort
import com.github.exact7.xtra.model.kraken.follows.Sort.ALPHABETICALLY
import com.github.exact7.xtra.model.kraken.follows.Sort.FOLLOWED_AT
import com.github.exact7.xtra.model.kraken.follows.Sort.LAST_BROADCAST
import com.github.exact7.xtra.ui.common.ExpandingBottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_followed_channels_sort.*

class FollowedChannelsSortDialog : ExpandingBottomSheetDialogFragment() {

    interface OnFilter {
        fun onChange(sort: Sort, sortText: CharSequence, order: Order, orderText: CharSequence)
    }

    companion object {

        private const val SORT = "sort"
        private const val ORDER = "order"

        fun newInstance(sort: Sort, order: Order): FollowedChannelsSortDialog {
            return FollowedChannelsSortDialog().apply {
                arguments = bundleOf(SORT to sort, ORDER to order)
            }
        }
    }

    private lateinit var listener: OnFilter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnFilter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_followed_channels_sort, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val originalSortId = when (args.getSerializable(SORT) as Sort) {
            FOLLOWED_AT -> R.id.time_followed
            ALPHABETICALLY -> R.id.alphabetically
            LAST_BROADCAST -> R.id.last_broadcast
        }
        val originalOrderId = if (args.getSerializable(ORDER) as Order == DESC) R.id.newest_first else R.id.oldest_first
        sort.check(originalSortId)
        order.check(originalOrderId)
        apply.setOnClickListener {
            val checkedSortId = sort.checkedRadioButtonId
            val checkedOrderId = order.checkedRadioButtonId
            if (checkedSortId != originalSortId || checkedOrderId != originalOrderId) {
                val sortBtn = view.findViewById<RadioButton>(checkedSortId)
                val orderBtn = view.findViewById<RadioButton>(checkedOrderId)
                listener.onChange(
                        when (checkedSortId) {
                            R.id.time_followed -> FOLLOWED_AT
                            R.id.alphabetically -> ALPHABETICALLY
                            else -> LAST_BROADCAST
                        },
                        sortBtn.text,
                        if (checkedOrderId == R.id.newest_first) DESC else ASC,
                        orderBtn.text)
            }
            dismiss()
        }
    }
}