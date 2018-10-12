package com.exact.xtra.ui.videos

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.exact.xtra.R
import com.exact.xtra.model.game.Game
import com.exact.xtra.util.C
import kotlinx.android.synthetic.main.fragment_videos.*

class GameVideosFragment : BaseVideosFragment(), VideosSortDialog.OnFilterApplied {

    private companion object {
        const val SORT_TAG = "GameVideosSort"
        const val PERIOD_TAG = "GameVideosPeriod"
        val DEFAULT_SORT_ID = Sort.VIEWS.value
        val DEFAULT_PERIOD_ID = Period.WEEK.value
    }

    private lateinit var game: Game
    private lateinit var prefs: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game = arguments?.getParcelable("game")!!
        sortBar.setOnClickListener { VideosSortDialog.newInstance(viewModel.sort, viewModel.period!!).show(childFragmentManager, null) }
    }

    override fun initializeViewModel() {
        prefs = requireActivity().getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
        viewModel.sort = Sort.valueOf(prefs.getString(SORT_TAG, DEFAULT_SORT_ID)!!.toUpperCase())
        val sortStringId = if (viewModel.sort == Sort.VIEWS) {
            R.string.view_count
        } else {
            R.string.upload_date
        }

        viewModel.period = Period.valueOf(prefs.getString(PERIOD_TAG, DEFAULT_PERIOD_ID)!!.toUpperCase())
        val periodStringId = when (viewModel.period!!) {
            Period.DAY -> R.string.today
            Period.WEEK ->  R.string.this_week
            Period.MONTH -> R.string.this_month
            Period.ALL -> R.string.all_time
        }
        setSortText(getString(sortStringId), getString(periodStringId))
    }

    override fun loadData(override: Boolean) {
        viewModel.loadVideos(game = game.info.name, reload = override)
    }

    override fun onApply(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        var shouldReload = false
        val editor = prefs.edit()
        if (viewModel.sort != sort) {
            shouldReload = true
            viewModel.sort = sort
            editor.putString(SORT_TAG, sort.value)
        }
        if (viewModel.period != period) {
            shouldReload = true
            viewModel.period = period
            editor.putString(PERIOD_TAG, period.value)
        }
        if (shouldReload) {
            editor.apply()
            setSortText(sortText, periodText)
            loadData(true)
        }
    }

    private fun setSortText(sort: CharSequence, period: CharSequence) {
        viewModel.sortText.postValue(getString(R.string.sort_and_period, sort, period))
    }
}
