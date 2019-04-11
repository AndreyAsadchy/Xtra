package com.github.exact7.xtra.ui.view.chat

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.model.chat.Emote
import com.github.exact7.xtra.ui.common.ChatAdapter
import com.github.exact7.xtra.util.isGone
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
    private var isChatTouched: Boolean = false

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

    fun addEmotes(list: List<Emote>) {
        adapter.addEmotes(list)
    }

    fun setUsername(username: String) {
        adapter.setUsername(username)
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

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }
}
