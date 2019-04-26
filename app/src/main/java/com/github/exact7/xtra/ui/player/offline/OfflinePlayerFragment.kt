package com.github.exact7.xtra.ui.player.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import kotlinx.android.synthetic.main.fragment_player_offline.*

class OfflinePlayerFragment : BasePlayerFragment() {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override lateinit var viewModel: OfflinePlayerViewModel
    override val channel: Channel
        get() = null!!

    override fun onCreate(savedInstanceState: Bundle?) {
        enableNetworkCheck = false
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_offline, container, false)
    }

    override fun initialize() {
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(OfflinePlayerViewModel::class.java)
        playerView.player = viewModel.player
        viewModel.setVideo(requireArguments().getParcelable("video")!!)
    }

    override fun onNetworkRestored() {
        //do nothing
    }

    override fun onMovedToForeground() {
        if (this::viewModel.isInitialized && !wasInPictureInPictureMode) {
            viewModel.onResume()
        }
    }

    override fun onMovedToBackground() {
        if (this::viewModel.isInitialized && !wasInPictureInPictureMode) {
            viewModel.onPause()
        }
    }
}
