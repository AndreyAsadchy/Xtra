package com.github.exact7.xtra.ui.common

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class AlertDialogFragment : DialogFragment() {

    interface OnDialogResultListener {
        fun onDialogResult(requestCode: Int, resultCode: Int)
    }

    private var listener: OnDialogResultListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? OnDialogResultListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val builder = AlertDialog.Builder(requireContext())
        args.getString(KEY_TITLE)?.let(builder::setTitle)
        args.getString(KEY_MESSAGE)?.let(builder::setMessage)
        builder.setPositiveButton(args.getString(KEY_POSITIVE)) { _, _ ->
            listener?.onDialogResult(args.getInt(KEY_REQUEST_CODE), RESULT_POSITIVE)
            dismiss()
        }
        args.getString(KEY_NEGATIVE)?.let {
            builder.setNegativeButton(it) { _, _ ->
                listener?.onDialogResult(args.getInt(KEY_REQUEST_CODE), RESULT_NEGATIVE)
                dismiss()
            }
        }
        args.getString(KEY_NEUTRAL)?.let {
            builder.setNeutralButton(it) { _, _ ->
                listener?.onDialogResult(args.getInt(KEY_REQUEST_CODE), RESULT_NEUTRAL)
                dismiss()
            }
        }
        builder.setCancelable(args.getBoolean(KEY_CANCELABLE))
        return builder.create()
    }

    companion object {
        const val RESULT_POSITIVE = 0
        const val RESULT_NEGATIVE = 1
        const val RESULT_NEUTRAL = 2

        private const val KEY_REQUEST_CODE = "requestCode"
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_POSITIVE = "positive"
        private const val KEY_NEGATIVE = "negative"
        private const val KEY_NEUTRAL = "neutral"
        private const val KEY_CANCELABLE = "cancelable"

        fun show(fragmentManager: FragmentManager, requestCode: Int = 0, title: String? = null, message: String? = null, positiveButton: String, negativeButton: String? = null, neutralButton: String? = null, cancelable: Boolean = true) {
            AlertDialogFragment().apply {
                arguments = bundleOf(KEY_REQUEST_CODE to requestCode, KEY_TITLE to title, KEY_MESSAGE to message, KEY_POSITIVE to positiveButton, KEY_NEGATIVE to negativeButton, KEY_NEUTRAL to neutralButton, KEY_CANCELABLE to cancelable)
                show(fragmentManager, null)
            }
        }
    }
}