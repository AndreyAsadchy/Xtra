package com.github.exact7.xtra.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.main.MainActivity
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
        editText.setOnFocusChangeListener { _, hasFocus ->  (context as MainActivity).window.setSoftInputMode(if (hasFocus) WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE else WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) }
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
