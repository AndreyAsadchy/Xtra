package com.github.andreyasadchy.xtra.ui.player

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.github.andreyasadchy.xtra.R
import kotlinx.android.synthetic.main.dialog_sleep_timer.view.*

class SleepTimerDialog : DialogFragment() {

    interface OnSleepTimerStartedListener {
        fun onSleepTimerChanged(durationMs: Long, hours: Int, minutes: Int)
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
        val builder = AlertDialog.Builder(context)
                .setTitle(getString(R.string.sleep_timer))
                .setView(LayoutInflater.from(context).inflate(R.layout.dialog_sleep_timer, null).also { dialogView = it })
        val positiveListener: (dialog: DialogInterface, which: Int) -> Unit = { _, _ ->
            listener.onSleepTimerChanged(dialogView.hours.value * 3600_000L + dialogView.minutes.value * 60_000L,  dialogView.hours.value, dialogView.minutes.value)
            dismiss()
        }
        if (requireArguments().getLong(KEY_TIME_LEFT) < 0L) {
            builder.setPositiveButton(getString(R.string.start), positiveListener)
            builder.setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
        } else {
            builder.setPositiveButton(getString(R.string.set), positiveListener)
            builder.setNegativeButton(getString(R.string.stop)) { _, _ ->
                listener.onSleepTimerChanged(-1L, 0, 0)
                dismiss()
            }
            builder.setNeutralButton(android.R.string.cancel) { _, _ -> dismiss() }
        }
        return builder.create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialogView.hours.apply {
            minValue = 0
            maxValue = 23
        }
        dialogView.minutes.apply {
            minValue = 0
            maxValue = 59
        }
        requireArguments().getLong(KEY_TIME_LEFT).let {
            if (it < 0L) {
                dialogView.minutes.value = 15
            } else {
                val hours = it / 3600_000L
                dialogView.hours.value = hours.toInt()
                dialogView.minutes.value = ((it - hours * 3600_000L) / 60_000L).toInt()
            }
        }
    }

    companion object {
        private const val KEY_TIME_LEFT = "timeLeft"

        fun show(fragmentManager: FragmentManager, timeLeft: Long) {
            SleepTimerDialog().apply {
                arguments = bundleOf(KEY_TIME_LEFT to timeLeft)
                show(fragmentManager, null)
            }
        }
    }
}