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
        view_chat_et_message.setOnEditorActionListener { v, actionId, _ ->
            var handled = false
            if (callback != null) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    callback!!.send(v.text.toString())
                    v.text = ""
                    handled = true
                }
            }
            handled
        }
        view_chat_btn_send.setOnClickListener {
            if (callback != null) {
                callback!!.send(view_chat_et_message.text.toString())
                view_chat_et_message.setText("")
            }
        }
    }

    fun setCallback(callback: MessageSenderCallback) {
        this.callback = callback
    }
}
