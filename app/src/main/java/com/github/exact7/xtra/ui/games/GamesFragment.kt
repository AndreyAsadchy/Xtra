package com.github.exact7.xtra.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentGamesBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.pagers.GamePagerFragment
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_games.*
import javax.inject.Inject

class GamesFragment : Fragment(), Injectable, Scrollable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentGamesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentGamesBinding.inflate(inflater, container, false).let {
            binding = it
            it.setLifecycleOwner(this@GamesFragment)
            binding.root
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(GamesViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = GamesAdapter {
            (requireActivity() as MainActivity).fragNavController.pushFragment(GamePagerFragment.newInstance(it))
        }
        recyclerViewLayout.recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
