package com.exact.xtra.ui.videos

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.exact.xtra.R
import com.exact.xtra.model.game.Game
import com.exact.xtra.util.C
import kotlinx.android.synthetic.main.fragment_videos.*
import kotlinx.android.synthetic.main.sort_bar.view.*

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            if (!viewModel.isInitialized()) {
                prefs = requireActivity().getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
                viewModel.sort = Sort.valueOf(prefs.getString(SORT_TAG, DEFAULT_SORT_ID)!!.toUpperCase())
                val sortStringId = if (viewModel.sort == Sort.VIEWS) {
                    R.string.view_count
                } else {
                    R.string.upload_date
                }
                viewModel.sortText.value = getString(sortStringId)

                viewModel.period = Period.valueOf(prefs.getString(PERIOD_TAG, DEFAULT_PERIOD_ID)!!.toUpperCase())
                val periodStringId = when (viewModel.period!!) {
                    Period.DAY -> R.string.today
                    Period.WEEK ->  R.string.this_week
                    Period.MONTH -> R.string.this_month
                    Period.ALL -> R.string.all_time
                }
                viewModel.periodText.value = getString(periodStringId)
            }
            val observer = Observer<CharSequence> { sortBar.sortText.text = getString(R.string.sort_and_period, viewModel.sortText.value, viewModel.periodText.value) }
            viewModel.sortText.observe(this, observer)
            viewModel.periodText.observe(this, observer)
        }
    }

    override fun loadData(override: Boolean) {
        viewModel.loadVideos(game = game.info.name, reload = override)
    }

    override fun onApply(sort: Sort, sortText: CharSequence, period: Period, periodText: CharSequence) {
        var shouldReload = false
        val editor = prefs.edit()
        if (viewModel.sortText.value != sortText) {
            shouldReload = true
            viewModel.sort = sort
            viewModel.sortText.value = sortText
            editor.putString(SORT_TAG, sort.value)
        }
        if (viewModel.periodText.value != periodText) {
            shouldReload = true
            viewModel.period = period
            viewModel.periodText.value = periodText
            editor.putString(PERIOD_TAG, period.value)
        }
        if (shouldReload) {
            editor.apply()
            loadData(true)
        }
    }
}
