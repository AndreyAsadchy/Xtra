package com.github.exact7.xtra.ui.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.databinding.DialogClipDownloadBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.DownloadUtils
import kotlinx.android.synthetic.main.dialog_clip_download.*
import kotlinx.android.synthetic.main.storage_selection.view.*
import javax.inject.Inject

class ClipDownloadDialog : DialogFragment(), Injectable {

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
            clip = arguments!!.getParcelable(KEY_CLIP)!!
            setQualities(arguments!!.getSerializable(KEY_QUALITIES) as Map<String, String>?)
            qualities.observe(viewLifecycleOwner, Observer {
                init(it)
            })
        }
    }

    private fun init(qualities: Map<String, String>) {
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_quality_item, qualities.keys.toTypedArray())
        cancel.setOnClickListener { dismiss() }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val sdCardPresent = DownloadUtils.isSdCardPresent
        if (sdCardPresent) {
            storageSelectionContainer.visibility = View.VISIBLE
            storageSelectionContainer.radioGroup.check(if (prefs.getBoolean(C.DOWNLOAD_STORAGE, true)) R.id.internalStorage else R.id.sdCard)
        }
        download.setOnClickListener {
            val quality = spinner.selectedItem.toString()
            val internalStorageSelected = storageSelectionContainer.radioGroup.checkedRadioButtonId != R.id.sdCard
            val toInternalStorage = !sdCardPresent || internalStorageSelected
            viewModel.download(qualities.getValue(quality), quality, toInternalStorage)
            if (sdCardPresent) {
                prefs.edit { putBoolean(C.DOWNLOAD_STORAGE, internalStorageSelected) }
            }
            dismiss()
        }
    }
}
