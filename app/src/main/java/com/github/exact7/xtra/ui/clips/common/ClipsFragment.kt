package com.github.exact7.xtra.ui.clips.common

import androidx.lifecycle.Observer
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.kraken.clip.Period
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.clips.BaseClipsFragment
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_clips.*

class ClipsFragment : BaseClipsFragment() {

    override lateinit var viewModel: ClipsViewModel
    val isChannel: Boolean
        get() = arguments?.getParcelable<Channel?>(C.CHANNEL) != null

    override fun initialize() {
        viewModel = createViewModel(ClipsViewModel::class.java)
        binding.viewModel = viewModel
        binding.sortText = viewModel.sortText
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
        viewModel.loadClips(arguments?.getParcelable<Channel?>(C.CHANNEL)?.name, arguments?.getParcelable<Game?>(C.GAME))
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }

    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        var period: Period? = null
        var trending = false
        when (tag) {
            R.string.trending -> trending = true
            R.string.today -> period = Period.DAY
            R.string.this_week -> period = Period.WEEK
            R.string.this_month -> period = Period.MONTH
            R.string.all_time -> period = Period.ALL
        }
        adapter.submitList(null)
        viewModel.filter(period, trending, index, text)
    }
}
