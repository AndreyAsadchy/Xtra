package com.exact.xtra.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.exact.xtra.R
import kotlinx.android.synthetic.main.dialog_clip_download.*

class ClipDownloadDialog : DialogFragment() {

    interface OnDownloadClickListener {
        fun onClick(quality: String)
    }

    companion object {
        private const val QUALITIES = "qualities"

        fun newInstance(qualities: List<CharSequence>): ClipDownloadDialog {
            return ClipDownloadDialog().apply {
                arguments = bundleOf(QUALITIES to qualities)
            }
        }
    }

    private lateinit var listener: OnDownloadClickListener

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        listener = parentFragment as OnDownloadClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_clip_download, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = requireActivity()
        spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, arguments!!.getCharSequenceArrayList(QUALITIES)!!)
        cancel.setOnClickListener { dismiss() }
        download.setOnClickListener {
            listener.onClick(spinner.selectedItem.toString())
            dismiss()
        }
    }
}
