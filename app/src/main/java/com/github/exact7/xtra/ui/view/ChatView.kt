package com.github.exact7.xtra.ui.view

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
import kotlinx.android.synthetic.main.view_chat.view.*
import java.util.LinkedList

const val MAX_MESSAGE_COUNT = 125

class ChatView : RelativeLayout {

    interface MessageSenderCallback {
        fun send(message: String)
    }

    private lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var isChatTouched: Boolean = false
    private var userNickname: String? = null

    var messageEnabled = false
        set(value) {
            messageView.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }
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
        recyclerView.itemAnimator = null
        layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isChatTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
                val showButton = shouldShowButton()
                val buttonShowing = isButtonShowing()
                if (!buttonShowing && showButton) {
                    btnDown.visibility = View.VISIBLE
                } else if (buttonShowing && !showButton) {
                    btnDown.visibility = View.GONE
                }
            }
        })
        btnDown.setOnClickListener {
            it.visibility = if (isButtonShowing()) View.GONE else View.VISIBLE
            post { recyclerView.scrollToPosition(getLastItemPosition()) }
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

    fun notifyAdapter() {
        adapter.notifyItemInserted(getLastItemPosition())
        if (adapter.itemCount > MAX_MESSAGE_COUNT) {
            adapter.messages.removeFirst()
            adapter.notifyItemRemoved(0)
        }
        if (!isChatTouched && !isButtonShowing()) {
            recyclerView.scrollToPosition(getLastItemPosition())
        }
    }

    fun submitList(list: LinkedList<ChatMessage>) {
        adapter = ChatAdapter(list, userNickname, context)
        recyclerView.adapter = adapter
    }

    fun addEmotes(list: List<Emote>) {
        adapter.addEmotes(list)
    }

    fun setUserNickname(nickname: String) {
        userNickname = nickname
    }

    private fun getLastItemPosition(): Int = adapter.itemCount - 1
    private fun isButtonShowing(): Boolean = btnDown.visibility == View.VISIBLE
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
