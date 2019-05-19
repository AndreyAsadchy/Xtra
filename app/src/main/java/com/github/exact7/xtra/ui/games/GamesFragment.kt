package com.github.exact7.xtra.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.github.exact7.xtra.databinding.FragmentGamesBinding
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.Scrollable
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_games.*

class GamesFragment : BaseNetworkFragment(), Scrollable {

    interface OnGameSelectedListener {
        fun openGame(game: Game)
    }

    override lateinit var viewModel: GamesViewModel
    private lateinit var adapter: GamesAdapter
    private lateinit var binding: FragmentGamesBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            FragmentGamesBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                binding.root
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = GamesAdapter(requireActivity() as MainActivity).also { adapter = it }
    }

    override fun initialize() {
        viewModel = createViewModel(GamesViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        search.setOnClickListener { (requireActivity() as MainActivity).openSearch() }
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}
