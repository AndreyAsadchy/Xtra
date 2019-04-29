package com.github.exact7.xtra.ui.view.chat

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.ui.common.ChatAdapter
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.util.gone
import com.github.exact7.xtra.util.hideKeyboard
import com.github.exact7.xtra.util.isGone
import com.github.exact7.xtra.util.isVisible
import com.github.exact7.xtra.util.toggleVisibility
import com.github.exact7.xtra.util.visible
import kotlinx.android.synthetic.main.view_chat.view.*

private const val MAX_MESSAGE_COUNT = 125

class ChatView : RelativeLayout {

    interface MessageSenderCallback {
        fun send(message: CharSequence)
    }

    private val adapter = ChatAdapter(context)

    private val layoutManager = LinearLayoutManager(context)
    private var isChatTouched = false

    private var emoteListsAddedCount = 0
    //    private var recentEmotes: List<Emote>? = null
    private var twitchEmotes: MutableList<Emote> = ArrayList()
    private var otherEmotes: MutableSet<Emote> = HashSet()

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
        chatRecyclerView.layoutTransition = LayoutTransition()

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
            editText.setText(text.substring(0, text.lastIndexOf(' ').let { if (it >= 0) it else 0 }))
        }
        send.setOnClickListener { sendMessage() }
        initEmotesViewPager()
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
        try {
            list?.also {
                when (list.firstOrNull()) {
                    is BttvEmote, is FfzEmote -> {
                        otherEmotes.addAll(it)
                        adapter.addEmotes(it)
                    }
                    else -> twitchEmotes.addAll(list)
                }
                viewPager.adapter?.notifyDataSetChanged()
            }
            if (++emoteListsAddedCount == 2 && otherEmotes.isNotEmpty()) {
                tabLayout.visible()
            }
        } catch (e: NullPointerException) {
            Crashlytics.logException(e)
            Crashlytics.log("ChatView.addEmotes: Adapter: $adapter. List is null: ${list == null}. First or null: ${list?.firstOrNull()}. View pager: $viewPager. Tab layout: $tabLayout")
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

    private fun sendMessage(): Boolean {
        editText.hideKeyboard()
        editText.clearFocus()
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
        val playerFragment = (context as MainActivity).playerFragment
        if (playerFragment == null) {
            Crashlytics.log("ChatView.initEmotesViewPager: playerFragment is null")
            postDelayed(this::initEmotesViewPager, 500L)
            return
        } else if (!playerFragment.isAdded) return //needed because we re-attach fragment after closing PIP
        viewPager.adapter = object : FragmentPagerAdapter(playerFragment.childFragmentManager) {

            override fun getItem(position: Int): Fragment {
                val list = when (position) {
                    0 -> twitchEmotes
                    else -> otherEmotes
                }
                return EmotesFragment.newInstance(list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }))
            }

            override fun getCount(): Int = 1 + if (otherEmotes.isNotEmpty()) 1 else 0

            override fun getPageTitle(position: Int): CharSequence? {
                return if (position == 0) {
                    "Twitch"
                } else {
                    "BTTV/FFZ"
                }
            }
        }
        emotes.setOnClickListener {
            //TODO add animation
            viewPager.toggleVisibility()
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
