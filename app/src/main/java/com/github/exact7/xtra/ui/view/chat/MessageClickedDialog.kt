package com.github.exact7.xtra.ui.view.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.kraken.Channel
import com.github.exact7.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.exact7.xtra.util.gone
import kotlinx.android.synthetic.main.dialog_chat_message_click.*
import javax.inject.Inject

class MessageClickedDialog : ExpandingBottomSheetDialogFragment(), Injectable {

    interface OnButtonClickListener {
        fun onReplyClicked(userName: String)
        fun onCopyMessageClicked(message: String)
        fun onViewProfileClicked(channel: Channel)
    }

    companion object {
        private const val KEY_MESSAGING = "messaging"
        private const val KEY_ORIGINAL = "original"
        private const val KEY_FORMATTED = "formatted"

        fun newInstance(messagingEnabled: Boolean, originalMessage: CharSequence, formattedMessage: CharSequence) = MessageClickedDialog().apply {
            arguments = bundleOf(KEY_MESSAGING to messagingEnabled, KEY_ORIGINAL to originalMessage, KEY_FORMATTED to formattedMessage)
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MessageClickedViewModel> { viewModelFactory }

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
        val args = requireArguments()
        message.text = args.getCharSequence(KEY_FORMATTED)!!
        val msg = args.getCharSequence(KEY_ORIGINAL)!!
        if (args.getBoolean(KEY_MESSAGING)) {
            reply.setOnClickListener {
                listener.onReplyClicked(extractUserName(msg))
                dismiss()
            }
            copyMessage.setOnClickListener {
                listener.onCopyMessageClicked(msg.substring(msg.indexOf(':') + 1))
                dismiss()
            }
        } else {
            reply.gone()
            copyMessage.gone()
        }
        viewProfile.setOnClickListener {
            viewModel.loadUser(extractUserName(msg)).observe(viewLifecycleOwner, Observer {
                listener.onViewProfileClicked(it)
                dismiss()
            })
        }
        viewModel.errors.observe(viewLifecycleOwner, Observer {
            Toast.makeText(context, getString(R.string.error_loading_user), Toast.LENGTH_SHORT).show()
        })
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