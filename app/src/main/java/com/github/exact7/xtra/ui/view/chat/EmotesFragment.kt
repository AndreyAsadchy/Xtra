package com.github.exact7.xtra.ui.view.chat

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.chat.ChatViewModel
import com.github.exact7.xtra.ui.view.GridAutofitLayoutManager
import com.github.exact7.xtra.util.convertDpToPixels


class EmotesFragment : Fragment() {

    private lateinit var listener: (Emote) -> Unit
    private lateinit var layoutManager: GridAutofitLayoutManager

    private val viewModel by viewModels<ChatViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (requireParentFragment() as ChatFragment)::appendEmote
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emotes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        val args = requireArguments()
        val emotesAdapter = EmotesAdapter(listener, args.getBoolean(KEY_ANIMATE_GIFS))
        with(view as RecyclerView) {
            itemAnimator = null
            adapter = emotesAdapter
            layoutManager = GridAutofitLayoutManager(context, context.convertDpToPixels(50f)).also { this@EmotesFragment.layoutManager = it }
        }
        val observer: Observer<List<Emote>> = Observer(emotesAdapter::submitList)
        when (args.getInt(KEY_POSITION)) {
            0 -> viewModel.recentEmotes.observe(viewLifecycleOwner, observer)
            1 -> viewModel.twitchEmotes.observe(viewLifecycleOwner, observer)
            else -> viewModel.otherEmotes.observe(viewLifecycleOwner, observer)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        layoutManager.updateWidth()
    }

    companion object {
        private const val KEY_POSITION = "position"
        private const val KEY_ANIMATE_GIFS = "animateGifs"

        fun newInstance(position: Int, animateGifs: Boolean) = EmotesFragment().apply { arguments = bundleOf("position" to position, KEY_ANIMATE_GIFS to animateGifs) }
    }
}