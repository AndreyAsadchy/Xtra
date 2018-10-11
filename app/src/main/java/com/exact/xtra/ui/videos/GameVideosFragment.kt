package com.exact.xtra.ui.videos

import android.content.Context
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        prefs = requireActivity().getSharedPreferences(C.USER_PREFS, MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game = arguments?.getParcelable("game")!!
        sortBar.setOnClickListener { VideosSortDialog.newInstance(prefs.getInt(SORT_TAG, DEFAULT_SORT_ID), prefs.getInt(PERIOD_TAG, DEFAULT_PERIOD_ID)).show(childFragmentManager, null) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isFragmentVisible) {
            if (!viewModel.isInitialized()) {

                viewModel.sort = Sort.valueOf(prefs.getString(SORT_TAG, DEFAULT_SORT_ID)!!)
                val sortStringId = if (viewModel.sort == Sort.VIEWS) {
                    R.string.view_count
                } else {
                    R.string.upload_date
                }
                viewModel.sortText.postValue(getString(sortStringId))

                val periodStringId: Int
                viewModel.period = when (prefs.getInt(PERIOD_TAG, DEFAULT_PERIOD_ID)) {
                    R.id.week -> {
                        periodStringId = R.string.this_week
                        Period.WEEK
                    }
                    R.id.month -> {
                        periodStringId = R.string.this_month
                        Period.MONTH
                    }
                    R.id.all -> {
                        periodStringId = R.string.all_time
                        Period.ALL
                    }
                    else -> throw IllegalStateException("Unknown period")
                }
                viewModel.periodText.postValue(getString(periodStringId))
            }
            val observer = Observer<CharSequence> { sortBar.sortText.text = getString(R.string.sort_and_period, viewModel.sortText.value, viewModel.periodText.value) }
            viewModel.sortText.observe(this, observer)
            viewModel.periodText.observe(this, observer)
        }
    }

    override fun loadData(override: Boolean) {
        viewModel.loadVideos(game = game.info.name, reload = override)
    }

    override fun onApply(sortId: Int, sortText: CharSequence, periodId: Int, periodText: CharSequence) {
        var shouldReload = false
        val editor = prefs.edit()
        if (viewModel.sortText.value != sortText) {
            shouldReload = true
            viewModel.sort = if (sortId == R.id.time) Sort.TIME else Sort.VIEWS
            viewModel.sortText.postValue(sortText)
            editor.putInt(SORT_TAG, sortId)
        }
        if (viewModel.periodText.value != periodText) {
            shouldReload = true
            viewModel.period = when (periodId) {
                R.id.week -> Period.WEEK
                R.id.month -> Period.MONTH
                R.id.all -> Period.ALL
                else -> null
            }
            viewModel.periodText.postValue(periodText)
            editor.putInt(PERIOD_TAG, periodId)
        }
        if (shouldReload) {
            editor.apply()
            loadData(true)
        }
    }
}
