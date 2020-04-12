package com.github.exact7.xtra.ui.player.offline

import android.os.Bundle
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.model.offline.OfflineVideo
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.ui.player.PlayerMode
import com.github.exact7.xtra.util.FragmentUtils

class OfflinePlayerFragment : BasePlayerFragment() {
//    override fun play(obj: Parcelable) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    override val viewModel by viewModels<OfflinePlayerViewModel> { viewModelFactory }
    override val channel: Channel
        get() = null!!

    override val layoutId: Int
        get() = R.layout.fragment_player_offline
    override val chatContainerId: Int
        get() = R.id.dummyView

    override val shouldEnterPictureInPicture: Boolean
        get() = viewModel.playerMode.value == PlayerMode.NORMAL

    override val controllerShowTimeoutMs: Int = 5000

    override fun onCreate(savedInstanceState: Bundle?) {
        enableNetworkCheck = false
        super.onCreate(savedInstanceState)
    }

    override fun initialize() {
        viewModel.setVideo(requireArguments().getParcelable(KEY_VIDEO)!!)
        super.initialize()
        requireView().findViewById<ImageButton>(R.id.settings).setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.qualities, viewModel.qualityIndex) }
    }

    override fun onNetworkRestored() {
        //do nothing
    }

    override fun onMovedToForeground() {
        viewModel.onResume()
    }

    override fun onMovedToBackground() {
        viewModel.onPause()
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }

    companion object {
        private const val KEY_VIDEO = "video"

        fun newInstance(video: OfflineVideo): OfflinePlayerFragment {
            return OfflinePlayerFragment().apply { arguments = bundleOf(KEY_VIDEO to video) }
        }
    }
}