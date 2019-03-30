package com.github.exact7.xtra.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.DialogClipDownloadBinding
import com.github.exact7.xtra.model.kraken.clip.Clip
import kotlinx.android.synthetic.main.dialog_clip_download.*
import javax.inject.Inject

class ClipDownloadDialog : BaseDownloadDialog() {

    companion object {
        private const val KEY_QUALITIES = "urls"
        private const val KEY_CLIP = "clip"

        fun newInstance(clip: Clip, qualities: Map<String, String>? = null): ClipDownloadDialog {
            return ClipDownloadDialog().apply {
                arguments = bundleOf(KEY_CLIP to clip, KEY_QUALITIES to qualities)
            }
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ClipDownloadViewModel
    private lateinit var binding: DialogClipDownloadBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?  =
            DialogClipDownloadBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                binding.root
            }

    @Suppress("UNCHECKED_CAST")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ClipDownloadViewModel::class.java)
        binding.viewModel = viewModel
        viewModel.apply {
            clip = requireArguments().getParcelable(KEY_CLIP)!!
            setQualities(requireArguments().getSerializable(KEY_QUALITIES) as Map<String, String>?)
            qualities.observe(viewLifecycleOwner, Observer {
                init(it)
            })
        }
    }

    private fun init(qualities: Map<String, String>) {
        val context = requireContext()
        init(context)
        spinner.adapter = ArrayAdapter(context, R.layout.spinner_quality_item, qualities.keys.toTypedArray())
        cancel.setOnClickListener { dismiss() }
        download.setOnClickListener {
            val quality = spinner.selectedItem.toString()
            viewModel.download(qualities.getValue(quality), downloadPath, quality)
            dismiss()
        }
    }
}
