package com.github.exact7.xtra.ui.player.clip

import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.ClipDownloadDialog
import com.github.exact7.xtra.ui.fragment.RadioButtonDialogFragment
import com.github.exact7.xtra.ui.player.BasePlayerFragment
import com.github.exact7.xtra.util.FragmentUtils
import kotlinx.android.synthetic.main.fragment_player_video.*
import kotlinx.android.synthetic.main.player_video.*

class ClipPlayerFragment : BasePlayerFragment(), RadioButtonDialogFragment.OnSortOptionChanged, ClipDownloadDialog.OnDownloadClickListener {
    override fun play(obj: Parcelable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private companion object {
        const val TAG = "ClipPlayer"
    }

    override lateinit var viewModel: ClipPlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_player_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        channelBtn.setOnClickListener(v -> channelListener.viewChannel(clip.getBroadcaster().getName()));
        //TODO morebtn
        settings.isEnabled = false
        download.isEnabled = false
        settings.setColorFilter(Color.GRAY) //TODO
        download.setColorFilter(Color.GRAY)
        settings.setOnClickListener { FragmentUtils.showRadioButtonDialogFragment(childFragmentManager, viewModel.helper.qualities.value!!, viewModel.helper.selectedQualityIndex) }
        download.setOnClickListener { ClipDownloadDialog.newInstance(viewModel.helper.qualities.value!!).show(childFragmentManager, null) }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ClipPlayerViewModel::class.java)
        playerView.player = viewModel.player
        viewModel.helper.qualities.observe(this, Observer {
            val loaded = it != null
            settings.isEnabled = loaded
            download.isEnabled = loaded
            settings.setColorFilter(Color.WHITE)
            download.setColorFilter(Color.WHITE)
        })
//        viewModel.helper.chatMessages.observe(this, Observer(chatView::submitList))
//        viewModel.helper.newMessage.observe(this, Observer { chatView.notifyAdapter() })
//        if (!viewModel.isInitialized()) {
//            viewModel.clip = arguments!!.getParcelable("clip")!!
//            viewModel.init()
//        }
    }

    override fun onClick(quality: String) {
        viewModel.download(quality)
    }

    override fun onChange(index: Int, text: CharSequence, tag: Int?) {
        viewModel.changeQuality(index)
    }
}
