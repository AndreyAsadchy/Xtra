package com.exact.xtra.ui.clips

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.view.View
import com.exact.xtra.R
import com.exact.xtra.model.game.Game
import com.exact.xtra.util.C
import com.exact.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_clips.*

class ClipsFragment : BaseClipsFragment() {

    private companion object {
        val sortOptions = listOf(R.string.trending, R.string.today, R.string.this_week, R.string.this_month, R.string.all_time)
        const val DEFAULT_INDEX = 2
        const val TAG = "Clips"
    }

    private var channelName: String? = null
    private var game: Game? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        channelName = arguments?.getString("channel")
        game = arguments?.getParcelable("game")
        sortBar.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(requireActivity(), childFragmentManager, sortOptions, DEFAULT_INDEX, TAG) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible && !viewModel.isInitialized()) { //TODO add init method in basefragment and only after that loaddata
            viewModel.sortText.postValue(getString(sortOptions[requireActivity().getSharedPreferences(C.USER_PREFS, MODE_PRIVATE).getInt(TAG, DEFAULT_INDEX)]))
            viewModel.period = Period.WEEK
        }
    }

    override fun loadData(override: Boolean) {
        viewModel.loadClips(channelName = channelName, gameName = game?.info?.name, reload = override)
    }

    override fun onSelect(index: Int, text: CharSequence, tag: Int?) {
        if (viewModel.sortText.value != text) {
            var period: Period? = null
            var trending = false
            when (tag) {
                R.string.trending -> trending = true
                R.string.today -> period = Period.DAY
                R.string.this_week -> period = Period.WEEK
                R.string.this_month -> period = Period.MONTH
                R.string.all_time -> period = Period.ALL
            }
            viewModel.period = period
            viewModel.trending = trending
            viewModel.sortText.postValue(text)
            loadData(true)
        }
    }
}
