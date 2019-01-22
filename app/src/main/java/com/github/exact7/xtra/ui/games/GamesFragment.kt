package com.github.exact7.xtra.ui.games

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.databinding.FragmentGamesBinding
import com.github.exact7.xtra.model.kraken.game.Game
import com.github.exact7.xtra.ui.common.BaseNetworkFragment
import com.github.exact7.xtra.ui.common.Scrollable
import kotlinx.android.synthetic.main.common_recycler_view_layout.view.*
import kotlinx.android.synthetic.main.fragment_games.*

class GamesFragment : BaseNetworkFragment(), Scrollable {

    interface OnGameSelectedListener {
        fun openGame(game: Game)
    }

    private lateinit var viewModel: GamesViewModel
    private lateinit var binding: FragmentGamesBinding
    private var listener: OnGameSelectedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnGameSelectedListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnGameSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            FragmentGamesBinding.inflate(inflater, container, false).let {
                binding = it
                it.setLifecycleOwner(viewLifecycleOwner)
                binding.root
            }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(GamesViewModel::class.java)
        binding.viewModel = viewModel
        val adapter = GamesAdapter(listener!!)
        recyclerViewLayout.recyclerView.adapter = adapter
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    override fun initialize() {
        //Automatic
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }

    override fun scrollToTop() {
        recyclerViewLayout.recyclerView.scrollToPosition(0)
    }
}
