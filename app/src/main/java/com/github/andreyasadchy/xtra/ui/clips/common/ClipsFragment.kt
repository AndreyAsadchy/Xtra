package com.github.andreyasadchy.xtra.ui.clips.common

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.kraken.Channel
import com.github.andreyasadchy.xtra.model.kraken.clip.Clip
import com.github.andreyasadchy.xtra.model.kraken.clip.Period
import com.github.andreyasadchy.xtra.model.kraken.game.Game
import com.github.andreyasadchy.xtra.ui.clips.BaseClipsFragment
import com.github.andreyasadchy.xtra.ui.clips.ClipsAdapter
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_clips.*
import kotlinx.android.synthetic.main.sort_bar.*

class ClipsFragment : BaseClipsFragment<ClipsViewModel>() {

    override val viewModel by viewModels<ClipsViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Clip> by lazy {
        val activity = requireActivity() as MainActivity
        val showDialog: (Clip) -> Unit = {
            lastSelectedItem = it
            showDownloadDialog()
        }
        if (arguments?.getParcelable<Channel?>(C.CHANNEL) != null) {
            ChannelClipsAdapter(this, activity, showDialog)
        } else {
            ClipsAdapter(this, activity, activity, showDialog)
        }
    }

    override fun initialize() {
        super.initialize()
        viewModel.sortText.observe(viewLifecycleOwner, Observer {
            sortText.text = it
        })
        viewModel.loadClips(arguments?.getParcelable<Channel?>(C.CHANNEL)?.name, arguments?.getParcelable<Game?>(C.GAME))
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireContext(), childFragmentManager, viewModel.sortOptions, viewModel.selectedIndex) }
    }

    override fun onChange(requestCode: Int, index: Int, text: CharSequence, tag: Int?) {
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
