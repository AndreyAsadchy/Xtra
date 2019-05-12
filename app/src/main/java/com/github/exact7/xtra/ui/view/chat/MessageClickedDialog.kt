package com.github.exact7.xtra.ui.view.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.ExpandingBottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_chat_message_click.*
import javax.inject.Inject

class MessageClickedDialog : ExpandingBottomSheetDialogFragment(), Injectable {

    interface OnButtonClickListener {
        fun onReplyClicked(userName: String)
        fun onCopyMessageClicked(message: String)
        fun onViewProfileClicked(channel: Channel)
    }

    companion object {
        private const val KEY_MESSAGE = "message"

        fun newInstance(message: CharSequence) = MessageClickedDialog().apply { arguments = bundleOf(KEY_MESSAGE to message) }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(MessageClickedViewModel::class.java) }

    private lateinit var listener: OnButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnButtonClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_chat_message_click, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val msg = requireArguments().getCharSequence(KEY_MESSAGE)!!
        message.text = msg
        reply.setOnClickListener {
            listener.onReplyClicked(extractUserName(msg))
            dismiss()
        }
        copyMessage.setOnClickListener {
            listener.onCopyMessageClicked(msg.substring(msg.indexOf(':') + 1))
            dismiss()
        }
        viewProfile.setOnClickListener {
            viewModel.loadUser(extractUserName(msg)).observe(viewLifecycleOwner, Observer {
                listener.onViewProfileClicked(it)
                dismiss()
            })
        }
    }

    private fun extractUserName(text: CharSequence): String {
        val userName = StringBuilder()
        for (c in text) {
            if (!c.isWhitespace()) {
                if (c != ':') {
                    userName.append(c)
                } else {
                    break
                }
            }
        }
        return userName.toString()
    }
}