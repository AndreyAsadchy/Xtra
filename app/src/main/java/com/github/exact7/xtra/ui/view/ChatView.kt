package com.github.exact7.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crashlytics.android.Crashlytics
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
    private var notTouched = true

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
        recyclerView.adapter = ChatAdapter(context).also { adapter = it }
        recyclerView.itemAnimator = null
        layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> isChatTouched = true
                MotionEvent.ACTION_UP -> {
                    isChatTouched = false
                    notTouched = false
                }
            }
            false
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val showButton = shouldShowButton()
                val buttonShowing = isButtonShowing()
                if (!buttonShowing && showButton) {
                    btnDown.visibility = View.VISIBLE
                } else if (buttonShowing && !showButton) {
                    btnDown.visibility = View.GONE
                }
            }
        })
        btnDown.setOnClickListener { post { recyclerView.scrollToPosition(getLastItemPosition()) } }


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
            try {
                adapter.messages.removeFirst()
                adapter.notifyItemRemoved(0)
            } catch (e: NoSuchElementException) {
                Crashlytics.log("ChatView.notifyAdapter NoSuchElementException. Adapter item count: ${adapter.itemCount}. Messages size: ${adapter.messages.size}. Error: ${e.message}")
            }
        }
        if (!isChatTouched && !isButtonShowing()) {
            recyclerView.scrollToPosition(getLastItemPosition())
        }
    }

    fun submitList(list: LinkedList<ChatMessage>) {
        adapter.messages = list
    }

    fun addEmotes(list: List<Emote>) {
        adapter.addEmotes(list)
    }

    fun setUserNickname(nickname: String) {
        adapter.setUserNickname(nickname)
    }

    private fun getLastItemPosition(): Int = adapter.itemCount - 1
    private fun isButtonShowing(): Boolean = btnDown.visibility == View.VISIBLE
    private fun shouldShowButton(): Boolean {
        if (notTouched) return false
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) {
            return false
        }
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100f * offset / (range - extent).toFloat())
        return percentage < 96f
    }

    fun setCallback(callback: MessageSenderCallback) {
        messageCallback = callback
    }
}
