package com.github.exact7.xtra.ui.view.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.MultiAutoCompleteTextView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.BttvEmote
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Chatter
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.model.chat.FfzEmote
import com.github.exact7.xtra.model.chat.RecentEmote
import com.github.exact7.xtra.ui.common.ChatAdapter
import com.github.exact7.xtra.util.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.auto_complete_emotes_list_item.view.*
import kotlinx.android.synthetic.main.view_chat.view.*
import kotlin.math.max
import com.github.exact7.xtra.model.kraken.user.Emote as TwitchEmote

const val MAX_ADAPTER_COUNT = 125
const val MAX_LIST_COUNT = MAX_ADAPTER_COUNT + 1

class ChatView : ConstraintLayout {

    interface MessageSenderCallback {
        fun send(message: CharSequence)
    }

    private val animateGifs = context.prefs().getBoolean(C.ANIMATED_EMOTES, true)
    private val adapter = ChatAdapter(context.convertDpToPixels(29.5f), context.convertDpToPixels(18.5f), animateGifs)

    private var isChatTouched = false

    private var hasRecentEmotes: Boolean? = null
    private var emotesAddedCount = 0

    private var autoCompleteList: MutableList<Any>? = null
    private var autoCompleteAdapter: AutoCompleteAdapter? = null

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

    private fun init(context: Context) {
        View.inflate(context, R.layout.view_chat, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.let {
            it.adapter = adapter
            it.itemAnimator = null
            it.layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                    btnDown.isVisible = shouldShowButton()
                }
            })
        }

        btnDown.setOnClickListener {
            post {
                recyclerView.scrollToPosition(adapter.messages!!.lastIndex)
                it.toggleVisibility()
            }
        }
    }

    fun submitList(list: MutableList<ChatMessage>) {
        adapter.messages = list
    }

    fun notifyMessageAdded() {
        adapter.messages!!.apply {
            adapter.notifyItemInserted(lastIndex)
            if (size >= MAX_LIST_COUNT) {
                val removeCount = size - MAX_ADAPTER_COUNT
                repeat(removeCount) {
                    removeAt(0)
                }
                adapter.notifyItemRangeRemoved(0, removeCount)
            }
            if (!isChatTouched && btnDown.isGone) {
                recyclerView.scrollToPosition(lastIndex)
            }
        }
    }

    fun addEmotes(list: List<Emote>) {
        when (list.firstOrNull()) {
            is BttvEmote, is FfzEmote -> {
                adapter.addEmotes(list)
                if (messagingEnabled) {
                    autoCompleteList!!.addAll(list)
                }
            }
            is TwitchEmote -> {
                if (messagingEnabled) {
                    autoCompleteList!!.addAll(list)
                }
            }
            is RecentEmote -> hasRecentEmotes = true
        }
        if (messagingEnabled && ++emotesAddedCount == 3) { //TODO refactor to not wait
            autoCompleteAdapter = AutoCompleteAdapter(context, autoCompleteList!!, animateGifs).apply {
                setNotifyOnChange(false)
                editText.setAdapter(this)

                var previousSize = 0
                editText.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus && count != previousSize) {
                        previousSize = count
                        notifyDataSetChanged()
                    }
                    setNotifyOnChange(hasFocus)
                }
            }
        }
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
    }

    fun setChatters(chatters: Collection<Chatter>) {
        autoCompleteList = chatters.toMutableList()
    }

    fun addChatter(chatter: Chatter) {
        autoCompleteAdapter?.add(chatter)
    }

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }

    fun hideEmotesMenu(): Boolean {
        return if (viewPager.isVisible) {
            viewPager.gone()
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

    fun enableChatInteraction(enableMessaging: Boolean, fragmentManager: FragmentManager) {
        this.fragmentManager = fragmentManager
        adapter.setOnClickListener { original, formatted ->
            editText.hideKeyboard()
            MessageClickedDialog.newInstance(enableMessaging, original, formatted).show(fragmentManager, null)
        }
        if (enableMessaging) {
            editText.addTextChangedListener(onTextChanged = { text, _, _, _ ->
                if (text?.isNotBlank() == true) {
                    send.visible()
                    clear.visible()
                } else {
                    send.gone()
                    clear.gone()
                }
            })
            editText.setTokenizer(SpaceTokenizer())
            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessage()
                } else {
                    false
                }
            }
            clear.setOnClickListener {
                val text = editText.text.toString().trimEnd()
                editText.setText(text.substring(0, max(text.lastIndexOf(' '), 0)))
                editText.setSelection(editText.length())
            }
            clear.setOnLongClickListener {
                editText.text.clear()
                true
            }
            send.setOnClickListener { sendMessage() }
            messageView.visible()
            viewPager.adapter = object : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

                override fun getItem(position: Int): Fragment {
                    return EmotesFragment.newInstance(position, animateGifs)
                }

                override fun getCount(): Int = 3

                override fun getPageTitle(position: Int): CharSequence? {
                    return when (position) {
                        0 -> context.getString(R.string.recent_emotes)
                        1 -> "Twitch"
                        else -> "BTTV/FFZ"
                    }
                }
            }
            viewPager.offscreenPageLimit = 2
            emotes.setOnClickListener {
                //TODO add animation
                with(viewPager) {
                    if (isGone) {
                        if (hasRecentEmotes != true && currentItem == 0) {
                            setCurrentItem(1, false)
                        }
                        visible()
                    } else {
                        gone()
                    }
                }
            }
            messagingEnabled = true
        }
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

    class SpaceTokenizer : MultiAutoCompleteTextView.Tokenizer {

        override fun findTokenStart(text: CharSequence, cursor: Int): Int {
            var i = cursor

            while (i > 0 && text[i - 1] != ' ') {
                i--
            }
            while (i < cursor && text[i] == ' ') {
                i++
            }

            return i
        }

        override fun findTokenEnd(text: CharSequence, cursor: Int): Int {
            var i = cursor
            val len = text.length

            while (i < len) {
                if (text[i] == ' ') {
                    return i
                } else {
                    i++
                }
            }

            return len
        }

        override fun terminateToken(text: CharSequence): CharSequence {
            return "${if (text.startsWith(':')) text.substring(1) else text} "
        }
    }

    class AutoCompleteAdapter(context: Context, list: List<Any>, private val animateGifs: Boolean) : ArrayAdapter<Any>(context, 0, list) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val viewHolder: ViewHolder

            val item = getItem(position)!!
            return when (getItemViewType(position)) {
                TYPE_EMOTE -> {
                    if (convertView == null) {
                        val view = LayoutInflater.from(context).inflate(R.layout.auto_complete_emotes_list_item, parent, false)
                        viewHolder = ViewHolder(view).also { view.tag = it }
                    } else {
                        viewHolder = convertView.tag as ViewHolder
                    }
                    viewHolder.containerView.apply {
                        item as Emote
                        if (animateGifs) {
                            image.loadImage(item.url)
                        } else {
                            image.loadBitmap(item.url)
                        }
                        name.text = item.name
                    }
                }
                else -> {
                    if (convertView == null) {
                        val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
                        viewHolder = ViewHolder(view).also { view.tag = it }
                    } else {
                        viewHolder = convertView.tag as ViewHolder
                    }
                    (viewHolder.containerView as TextView).apply {
                        text = (item as Chatter).name
                    }
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return if (getItem(position) is Emote) TYPE_EMOTE else TYPE_USERNAME
        }

        override fun getViewTypeCount(): Int = 2

        class ViewHolder(override val containerView: View) : LayoutContainer

        private companion object {
            const val TYPE_EMOTE = 0
            const val TYPE_USERNAME = 1
        }
    }
}
