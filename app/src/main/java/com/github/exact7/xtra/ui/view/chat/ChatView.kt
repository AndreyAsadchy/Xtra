package com.github.exact7.xtra.ui.view.chat

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.ui.common.ChatAdapter
import com.github.exact7.xtra.ui.common.MarginItemDecoration
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.streams.EmotesAdapter
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.isGone
import com.github.exact7.xtra.util.isVisible
import com.github.exact7.xtra.util.toggleVisibility
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.view_chat.view.*

private const val MAX_MESSAGE_COUNT = 125

class ChatView : RelativeLayout {

    interface MessageSenderCallback {
        fun send(message: String)
    }

    private val adapter = ChatAdapter(context)

    private val layoutManager = LinearLayoutManager(context)
    private var isChatTouched = false

    private var emoteListsAddedCount = 0
    //    private var recentEmotes: List<Emote>? = null
    private var twitchEmotes: List<com.github.exact7.xtra.model.kraken.user.Emote>? = null
    private var otherEmotes: HashSet<Emote>? = null

    private var messageCallback: MessageSenderCallback? = null
    var messagingEnabled = false
        set(value) {
            messageView.visible(value)
            field = value
        }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_chat, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        layoutTransition = LayoutTransition()
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                btnDown.visible(shouldShowButton())
            }
        })
        btnDown.setOnClickListener {
            post {
                recyclerView.scrollToPosition(getLastItemPosition())
                it.toggleVisibility()
            }
        }


        editText.setOnEditorActionListener { v, actionId, _ ->
            var handled = false
            messageCallback?.let {
                if (actionId == EditorInfo.IME_ACTION_SEND && v.text.isNotEmpty()) {
                    it.send(v.text.toString())
                    editText.text.clear()
                    handled = true
                }
            }
            handled
        }
        editText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val notEmpty = text?.isNotEmpty() == true
            send.visible(notEmpty)
            clear.visible(notEmpty)
        })
        clear.setOnClickListener {
            val text = editText.text.toString().trimEnd()
            editText.setText(text.substring(0, text.lastIndexOf(' ').let { if (it >= 0) it else 0 }))
        }
        send.setOnClickListener {
            messageCallback?.let {
                val text = editText.text
                if (text.isNotEmpty()) {
                    it.send(text.toString())
                    text.clear()
                }
            }
        }
    }

    fun submitList(list: MutableList<ChatMessage>) {
        adapter.messages = list
    }

    fun notifyMessageAdded() {
        adapter.messages?.let {
            adapter.notifyItemInserted(getLastItemPosition())
            if (getLastItemPosition() > MAX_MESSAGE_COUNT) {
                it.removeAt(0)
                adapter.notifyItemRemoved(0)
            }
            if (!isChatTouched && btnDown.isGone()) {
                recyclerView.scrollToPosition(getLastItemPosition())
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun addEmotes(list: List<Emote>?) {
        list?.let {
            adapter.addEmotes(it)
            when (list.firstOrNull()) {
                is BttvEmote, is FfzEmote -> {
                    if (otherEmotes == null) {
                        otherEmotes = it.toHashSet()
                    } else {
                        otherEmotes?.addAll(it)
                    }
                }
                else -> twitchEmotes = list as List<com.github.exact7.xtra.model.kraken.user.Emote>
            }
        }
        if (++emoteListsAddedCount == 3) {
            initEmotesViewPager()
        }
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }

    fun hideEmotesMenu(): Boolean {
        return if (viewPager.isVisible()) {
            viewPager.gone()
            true
        } else {
            false
        }
    }

    private fun initEmotesViewPager() {
        val size = 1 + if (otherEmotes != null) 1 else 0
        if (size == 1) {
            tabLayout.gone()
        }
        viewPager.adapter = object : FragmentPagerAdapter((context as MainActivity).playerFragment!!.childFragmentManager) {

            override fun getItem(position: Int): Fragment {
                val list: Collection<Emote>? = when (position) {
                    0 -> twitchEmotes
                    else -> otherEmotes
                }
                return EmotesFragment.newInstance(list!!.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }))
            }

            override fun getCount(): Int = size

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position == 0) {
                    "Twitch"
                } else {
                    "BTTV/FFZ"
                }
            }
        }
        emotes.setOnClickListener { viewPager.toggleVisibility() }
    }

    private fun getLastItemPosition(): Int = adapter.itemCount - 1

    private fun shouldShowButton(): Boolean {
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) {
            return false
        }
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100f * offset / (range - extent).toFloat())
        return percentage < 97f
    }
}
