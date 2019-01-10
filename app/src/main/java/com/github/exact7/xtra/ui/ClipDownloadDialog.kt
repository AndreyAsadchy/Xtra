package com.github.exact7.xtra.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.model.kraken.clip.Clip
import com.github.exact7.xtra.model.offline.ClipRequest
import com.github.exact7.xtra.repository.PlayerRepository
import com.github.exact7.xtra.service.DownloadWorker
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.dialog_clip_download.*
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

    @Inject
    lateinit var repository: PlayerRepository
    private var compositeDisposable: CompositeDisposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_clip_download, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        @Suppress("UNCHECKED_CAST") val qualities = arguments!!.getSerializable(KEY_QUALITIES) as Map<String, String>?
        val clip = arguments!!.getParcelable<Clip>(KEY_CLIP)!!
        if (qualities == null) {
            compositeDisposable = CompositeDisposable()
            repository.fetchClipQualities(clip.slug)
                    .subscribe({
                        init(clip, it)
                    }, {

                    })
                    .addTo(compositeDisposable!!)

        } else {
            init(clip, qualities)
        }
    }

    override fun onDestroy() {
        compositeDisposable?.clear()
        super.onDestroy()
    }

    private fun init(clip: Clip, qualities: Map<String, String>) {
        progressBar.visibility = View.GONE
        container.visibility = View.VISIBLE
        spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_quality_item, qualities.keys.toTypedArray())
        cancel.setOnClickListener { dismiss() }
        download.setOnClickListener {
            val quality = spinner.selectedItem.toString()
            DownloadWorker.download(Gson().toJson(ClipRequest(clip, qualities[quality]!!, quality)), false)
            dismiss()
        }
    }
}
