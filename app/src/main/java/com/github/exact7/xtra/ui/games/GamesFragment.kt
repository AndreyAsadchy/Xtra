package com.github.exact7.xtra.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.exact7.xtra.databinding.FragmentGamesBinding
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_games.*
import kotlinx.android.synthetic.main.fragment_search.*

class GamesFragment : BaseNetworkFragment(), Scrollable {

    interface OnGameSelectedListener {
        fun openGame(game: Game)
    }

    override lateinit var viewModel: GamesViewModel
    private lateinit var binding: FragmentGamesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            FragmentGamesBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                binding.root
            }

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GamesViewModel::class.java)
        binding.viewModel = viewModel
        val activity = requireActivity() as MainActivity
        val adapter = GamesAdapter(activity)
        recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        search.setOnClickListener { activity.openSearch() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}
