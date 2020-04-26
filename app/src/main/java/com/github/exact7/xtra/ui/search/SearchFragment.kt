package com.github.exact7.xtra.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.Utils
import com.github.exact7.xtra.ui.common.pagers.MediaPagerFragment
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : MediaPagerFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val adapter = SearchPagerAdapter(activity, childFragmentManager).apply {
            setOnItemChangedListener {
                if (it.isResumed) {
                    (it as Searchable).search(search.query.toString())
                }
            }
        }
        setAdapter(adapter)
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        search.isIconified = false
    }

    override fun initialize() {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            private var job: Job? = null

            override fun onQueryTextSubmit(query: String): Boolean {
                (currentFragment as? Searchable)?.search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                job?.cancel()
                if (newText.isNotEmpty()) {
                    job = lifecycleScope.launch {
                        delay(750)
                        (currentFragment as? Searchable)?.search(newText)
                    }
                } else {
                    (currentFragment as? Searchable)?.search(newText) //might be null on rotation, so as?
                }
                return false
            }
        })
    }

    override fun onNetworkRestored() {

    }
}