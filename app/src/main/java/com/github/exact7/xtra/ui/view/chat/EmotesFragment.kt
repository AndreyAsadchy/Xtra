package com.github.exact7.xtra.ui.view.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.view.GridAutofitLayoutManager
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.convertDpToPixels
import com.github.exact7.xtra.util.prefs
import com.github.exact7.xtra.model.kraken.user.Emote as TwitchEmote


class EmotesFragment : Fragment() {

    companion object {
        fun newInstance(emotes: List<Emote>) = EmotesFragment().apply { arguments = bundleOf("list" to emotes) }
    }

    private lateinit var listener: (Emote) -> Unit
    private val recyclerView by lazy { requireView() as RecyclerView }
    private var animateGifs = true
    var type = 0
        private set

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (requireParentFragment() as ChatFragment)::appendEmote
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emotes, container, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()
        animateGifs = context.prefs().getBoolean(C.ANIMATED_EMOTES, true)
        val emotes = requireArguments().getSerializable("list") as List<Emote>
        type = when (emotes.firstOrNull()) {
            is TwitchEmote -> 1
            is BttvEmote, is FfzEmote -> 2
            else -> 0
        }
        setEmotes(emotes)
        recyclerView.layoutManager = GridAutofitLayoutManager(context, context.convertDpToPixels(50f))
    }

    fun setEmotes(list: List<Emote>) {
        recyclerView.adapter = EmotesAdapter(list, listener, animateGifs)
    }
}