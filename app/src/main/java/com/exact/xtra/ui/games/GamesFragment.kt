package com.exact.xtra.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.exact.xtra.databinding.FragmentGamesBinding
import com.exact.xtra.di.Injectable
import com.exact.xtra.ui.Scrollable
import com.exact.xtra.ui.main.MainActivity
import com.exact.xtra.ui.pagers.GamePagerFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_games.*
import javax.inject.Inject

class GamesFragment : androidx.fragment.app.Fragment(), Injectable, Scrollable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentGamesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGamesBinding.inflate(inflater, container, false).apply { setLifecycleOwner(this@GamesFragment) }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewLayout.recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireActivity())
        recyclerViewLayout.recyclerView.addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(requireActivity(), androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(GamesViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = GamesAdapter {
            (requireActivity() as MainActivity).fragNavController.pushFragment(GamePagerFragment.newInstance(it))
        }
        recyclerViewLayout.recyclerView.adapter = adapter
        viewModel.list.observe(this, Observer {
            adapter.submitList(it)
        })
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
