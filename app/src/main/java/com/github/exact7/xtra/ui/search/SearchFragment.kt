package com.github.exact7.xtra.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.viewpager.widget.ViewPager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.Utils
import com.github.exact7.xtra.ui.common.pagers.MediaPagerFragment
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_media_pager.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : MediaPagerFragment() {

    private lateinit var adapter: SearchPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        adapter = SearchPagerAdapter(activity, childFragmentManager)
        setAdapter(adapter)
        toolbar.apply {
            navigationIcon = Utils.getNavigationIcon(activity)
            setNavigationOnClickListener { activity.popFragment() }
        }
        search.isIconified = false
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                (currentFragment as Searchable).search(search.query.toString())
            }

            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
        })
    }

    override fun initialize() {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            private var job: Job? = null

            override fun onQueryTextSubmit(query: String): Boolean {
                (currentFragment as Searchable).search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                job?.cancel()
                if (newText.isNotEmpty()) {
                    job = GlobalScope.launch {
                        delay(750)
                        withContext(Dispatchers.Main) {
                            (currentFragment as Searchable).search(newText)
                        }
                    }
                } else {
                    (currentFragment as Searchable).search(newText)
                }
                return false
            }
        })
    }

    override fun onNetworkRestored() {

    }
}