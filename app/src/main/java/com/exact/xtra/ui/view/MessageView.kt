package com.exact.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.exact.xtra.R
import kotlinx.android.synthetic.main.view_chat_message.view.*

class MessageView : LinearLayout {

    interface MessageSenderCallback {
        fun send(message: String)
    }

    private var callback: MessageSenderCallback? = null

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
        View.inflate(context, R.layout.view_chat_message, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        editText.setOnEditorActionListener { v, actionId, _ ->
            var handled = false
            if (callback != null) {
                if (actionId == EditorInfo.IME_ACTION_SEND && v.text.isNotEmpty()) {
                    callback!!.send(v.text.toString())
                    editText.text.clear()
                    handled = true
                }
            }
            handled
        }
        send.setOnClickListener {
            if (callback != null && editText.text.isNotEmpty()) {
                callback!!.send(editText.text.toString())
                editText.text.clear()
            }
        }
    }

    fun setCallback(callback: MessageSenderCallback) {
        this.callback = callback
    }
}
