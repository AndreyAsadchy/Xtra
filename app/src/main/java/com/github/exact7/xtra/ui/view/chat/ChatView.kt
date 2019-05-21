package com.github.exact7.xtra.ui.view.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.ui.chat.ChatFragment
import com.github.exact7.xtra.ui.common.ChatAdapter
import com.github.exact7.xtra.ui.streams.EmotesAdapter
import com.github.exact7.xtra.util.convertDpToPixels
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.hideKeyboard
import com.github.exact7.xtra.util.isGone
import com.github.exact7.xtra.util.isVisible
import com.github.exact7.xtra.util.showKeyboard
import com.github.exact7.xtra.util.toggleVisibility
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlin.math.max

private const val MAX_MESSAGE_COUNT = 125

class ChatView : ConstraintLayout {

    interface MessageSenderCallback {
        fun send(message: CharSequence)
    }

    private val adapter = ChatAdapter(context.convertDpToPixels(29.5f), context.convertDpToPixels(18.5f))

    private var isChatTouched = false

    private var twitchEmotes: MutableList<Emote> = ArrayList()
    private var otherEmotes: MutableSet<Emote> = HashSet()
    private var emotesAddedCount = 0

    private lateinit var fragmentManager: FragmentManager
    private var messagingEnabled = false

    private var messageCallback: MessageSenderCallback? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    init {
        val emotes: List<Emote> = ChatFragment.defaultBttvAndFfzEmotes()
        adapter.addEmotes(emotes)
        otherEmotes.addAll(emotes)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_chat, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.apply {
            adapter = this@ChatView.adapter
            itemAnimator = null
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    this@ChatView.btnDown.visible(shouldShowButton())
                }
            })
        }

        btnDown.setOnClickListener {
            post {
                recyclerView.scrollToPosition(getLastItemPosition())
                it.toggleVisibility()
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
            } else {
                false
            }
        }
        editText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
            val notBlank = text?.isNotBlank() == true
            send.visible(notBlank)
            clear.visible(notBlank)
        })
        clear.setOnClickListener {
            val text = editText.text.toString().trimEnd()
            editText.setText(text.substring(0, max(text.lastIndexOf(' '), 0)))
        }
        send.setOnClickListener { sendMessage() }
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

    fun addEmotes(list: List<Emote>?) {
        if (list?.isNotEmpty() == true) {
            when (list.first()) {
                is BttvEmote, is FfzEmote -> {
                    otherEmotes.addAll(list)
                    adapter.addEmotes(list)
                }
                else -> twitchEmotes.addAll(list)
            }
        }
        if (++emotesAddedCount == 3 && messagingEnabled) {
            initEmotesViewPager()
        }
    }

    fun setRecentEmotes(list: List<Emote>) {
        if (list.isNotEmpty()) {
            recentEmotes.apply {
                adapter = EmotesAdapter(list) { appendEmote(it) }
                visible()
            }
        }
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }

    fun hideEmotesMenu(): Boolean {
        return if (emotesMenu.isVisible()) {
            emotesMenu.gone()
            true
        } else {
            false
        }
    }

    fun appendEmote(emote: Emote) {
        editText.text.append(emote.name).append(' ')
    }

    @SuppressLint("SetTextI18n")
    fun reply(userName: CharSequence) {
        val text = "@$userName "
        editText.apply {
            setText(text)
            setSelection(text.length)
            showKeyboard()
        }
    }

    fun setMessage(text: CharSequence) {
        editText.setText(text)
    }

    fun enableMessaging(fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
        messagingEnabled = true
        adapter.setOnClickListener { original, formatted ->
            editText.hideKeyboard()
            MessageClickedDialog.newInstance(original, formatted).show(fragmentManager, null)
        }
        messageView.visible(true)
    }

    private fun sendMessage(): Boolean {
        editText.hideKeyboard()
        editText.clearFocus()
        hideEmotesMenu()
        return messageCallback?.let {
            val text = editText.text.trim()
            editText.text.clear()
            if (text.isNotEmpty()) {
                it.send(text)
                true
            } else {
                false
            }
        } == true
    }

    private fun initEmotesViewPager() {
        viewPager.adapter = object : FragmentStatePagerAdapter(fragmentManager) {

            override fun getItem(position: Int): Fragment {
                val list = when (position) {
                    0 -> twitchEmotes
                    else -> otherEmotes
                }
                return EmotesFragment.newInstance(list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }))
            }

            override fun getCount(): Int = 2

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "Twitch"
                    else -> "BTTV/FFZ"
                }
            }
        }
        emotes.setOnClickListener {
            //TODO add animation
            emotesMenu.toggleVisibility()
        }
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
