package com.exact.twitch.ui.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.exact.twitch.R
import com.exact.twitch.model.chat.ChatMessage
import com.exact.twitch.ui.adapter.ChatRecyclerViewAdapter
import kotlinx.android.synthetic.main.view_chat_recyclerview.view.*

class ChatRecyclerView : RelativeLayout {

    private lateinit var adapter: ChatRecyclerViewAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var isChatTouched: Boolean = false

    private val lastItemPosition: Int
        get() = adapter.itemCount - 1
    private val scrollOffsetItemCount: Int
        get() = lastItemPosition - layoutManager.findLastVisibleItemPosition()
    private val isButtonShowing: Boolean
        get() = view_chat_recyclerview_btn_down.visibility == View.VISIBLE

    companion object {
        private const val SCROLL_THRESHOLD = 5
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
        View.inflate(context, R.layout.view_chat_recyclerview, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        adapter = ChatRecyclerViewAdapter(context)
        view_chat_recyclerview_rv.adapter = adapter
        view_chat_recyclerview_rv.itemAnimator = null
        layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        view_chat_recyclerview_rv.layoutManager = layoutManager
        view_chat_recyclerview_rv.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> isChatTouched = true
                MotionEvent.ACTION_UP -> isChatTouched = false
            }
            false
        }
        view_chat_recyclerview_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val offset = scrollOffsetItemCount
                if (!isButtonShowing && offset >= SCROLL_THRESHOLD) {
                    view_chat_recyclerview_btn_down.visibility = View.VISIBLE
                } else if (isButtonShowing && offset < SCROLL_THRESHOLD) {
                    view_chat_recyclerview_btn_down.visibility = View.GONE
                }
            }
        })
        view_chat_recyclerview_btn_down.setOnClickListener { view_chat_recyclerview_rv.scrollToPosition(lastItemPosition) }
    }

    fun notifyAdapter() {
        val lastItemPosition = lastItemPosition
        adapter.notifyItemInserted(lastItemPosition)
        if (!isChatTouched && scrollOffsetItemCount < SCROLL_THRESHOLD) {
            view_chat_recyclerview_rv.scrollToPosition(lastItemPosition)
        }
    }

    fun submitList(list: List<ChatMessage>) {
        adapter.submitList(list)
    }
}
