package com.github.exact7.xtra.ui.player

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.github.exact7.xtra.R
import kotlinx.android.synthetic.main.dialog_sleep_timer.view.*

class SleepTimerDialog : DialogFragment() {

    interface OnSleepTimerStartedListener {
        fun onSleepTimerStarted(duration: Long)
    }

    private lateinit var listener: OnSleepTimerStartedListener
    private lateinit var dialogView: View

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnSleepTimerStartedListener
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return AlertDialog.Builder(context)
                .setTitle("Sleep timer")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    listener.onSleepTimerStarted(dialogView.hours.value * 3600_000L + dialogView.minutes.value * 60_000L)
                    dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
                .setView(LayoutInflater.from(context).inflate(R.layout.dialog_sleep_timer, null).also { dialogView = it })
                .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialogView.hours.apply {
            minValue = 0
            maxValue = 23
            wrapSelectorWheel = false
        }
        dialogView.minutes.apply {
            minValue = 0
            maxValue = 59
            value = 15
            wrapSelectorWheel = false
        }
    }
}