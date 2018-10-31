package com.github.exact7.xtra.ui.player.offline

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import kotlinx.android.synthetic.main.fragment_player_offline.*

class OfflinePlayerFragment : BasePlayerFragment() {
    override fun play(obj: Parcelable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override lateinit var viewModel: OfflinePlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_offline, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(OfflinePlayerViewModel::class.java)
        playerView.player = viewModel.player
        if (!viewModel.isInitialized()) {
            viewModel.video = arguments!!.getParcelable("video")!!
            viewModel.init()
        }
    }

}
