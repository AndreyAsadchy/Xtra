package com.github.exact7.xtra.ui.view.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.ui.streams.EmotesAdapter
import kotlinx.android.synthetic.main.view_chat.view.*
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.common.MarginItemDecoration
import com.github.exact7.xtra.ui.view.GridAutofitLayoutManager
import com.github.exact7.xtra.util.DisplayUtils


class EmotesFragment : Fragment() {

    companion object {
        fun newInstance(emotes: List<Emote>) = EmotesFragment().apply { arguments = bundleOf("list" to emotes) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emotes, container, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val editText = (view.parent.parent as RelativeLayout).editText
        val context = requireContext()
        (view as RecyclerView).apply {
            //            adapter = EmotesAdapter(requireArguments().getSerializable("list") as List<Emote>) { editText.text.append(it.name).append(' ') }
            adapter = EmotesAdapter(requireArguments().getSerializable("list") as List<Emote>) { println("CLICK ${it.name}") }
            layoutManager = GridAutofitLayoutManager(context, DisplayUtils.convertDpToPixels(context, 50f))
        }
    }
}