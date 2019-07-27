package com.github.exact7.xtra.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.Utils
import com.github.exact7.xtra.ui.common.pagers.MediaPagerFragment
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : MediaPagerFragment() {

    override lateinit var viewModel: SearchViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        setAdapter(SearchPagerAdapter(activity, childFragmentManager))
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        search.isIconified = false
    }

    override fun initialize() {
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.setQuery(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.setQuery(newText)
                return false
            }
        })
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }
}