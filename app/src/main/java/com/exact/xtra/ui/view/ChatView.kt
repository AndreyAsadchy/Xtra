package com.exact.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.exact.xtra.R
import com.exact.xtra.model.chat.ChatMessage
import com.exact.xtra.ui.common.ChatAdapter
import kotlinx.android.synthetic.main.view_chat.view.*

class ChatView : RelativeLayout {

    companion object {
        private const val SCROLL_THRESHOLD = 5
    }

    lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: androidx.recyclerview.widget.LinearLayoutManager
    private var isChatTouched: Boolean = false

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
        adapter = ChatAdapter()
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> isChatTouched = true
                MotionEvent.ACTION_UP -> isChatTouched = false
            }
            false
        }
        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val offset = getScrollOffsetItemCount()
                if (!isButtonShowing() && offset >= SCROLL_THRESHOLD) {
                    btnDown.visibility = View.VISIBLE
                } else if (isButtonShowing() && offset < SCROLL_THRESHOLD) {
                    btnDown.visibility = View.GONE
                }
            }
        })
        btnDown.setOnClickListener { recyclerView.scrollToPosition(getLastItemPosition()) }
    }

    fun notifyAdapter() {
        val lastItemPosition = getLastItemPosition()
        adapter.notifyItemInserted(lastItemPosition)
        if (!isChatTouched && getScrollOffsetItemCount() < SCROLL_THRESHOLD) {
            recyclerView.scrollToPosition(lastItemPosition)
        }
    }

    fun submitList(list: MutableList<ChatMessage>?) {
        adapter.submitList(list)
    }

    private fun getLastItemPosition(): Int = adapter.itemCount - 1
    private fun getScrollOffsetItemCount(): Int = getLastItemPosition() - layoutManager.findLastVisibleItemPosition()
    private fun isButtonShowing(): Boolean = btnDown.visibility == View.VISIBLE
}
