package com.github.exact7.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.chat.ChatMessage
import com.github.exact7.xtra.ui.common.ChatAdapter
import kotlinx.android.synthetic.main.view_chat.view.*


class ChatView : RelativeLayout {

    private companion object {
        const val MAX_MESSAGE_COUNT = 125
    }

    lateinit var adapter: ChatAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var isChatTouched: Boolean = false
    private var notTouched = true //TODO

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
        recyclerView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isChatTouched = true
                    notTouched = false
                }
                MotionEvent.ACTION_UP -> isChatTouched = false
            }
            false
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isButtonShowing() && shouldShowButton()) {
                    btnDown.visibility = View.VISIBLE
                } else if (isButtonShowing() && !shouldShowButton()) {
                    btnDown.visibility = View.GONE
                }
            }
        })
        btnDown.setOnClickListener { recyclerView.scrollToPosition(getLastItemPosition()) }
    }

    fun notifyAdapter() {
        adapter.notifyItemInserted(getLastItemPosition())
        if (adapter.itemCount > MAX_MESSAGE_COUNT) {
            adapter.messages.removeAt(0)
            adapter.notifyItemRemoved(0)
        }
        if (!isChatTouched && !shouldShowButton()) {
            recyclerView.scrollToPosition(getLastItemPosition())
        }
    }

    fun submitList(list: MutableList<ChatMessage>) {
        adapter = ChatAdapter(list)
        recyclerView.adapter = adapter
    }

    private fun getLastItemPosition(): Int = adapter.itemCount - 1
    private fun shouldShowButton(): Boolean {
        if (notTouched) return false
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) {
            return false
        }
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100 * offset / (range - extent).toFloat())
        return percentage < 92f
    }
    private fun isButtonShowing(): Boolean = btnDown.visibility == View.VISIBLE
}
