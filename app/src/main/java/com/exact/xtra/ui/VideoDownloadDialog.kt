package com.exact.xtra.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.exact.xtra.R
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey
import kotlinx.android.synthetic.main.dialog_video_download.*
import java.util.*

class VideoDownloadDialog(
        fragment: Fragment,
        private val qualities: List<CharSequence>,
        private val segments: List<HlsMediaPlaylist.Segment>?) : Dialog(fragment.requireActivity()), View.OnClickListener {

    private val listener: OnDownloadClickListener
    private lateinit var defaultRange: String

    interface OnDownloadClickListener {
        fun onClick(quality: String, keys: List<RenditionKey>)
    }

    init {
        listener = fragment as OnDownloadClickListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_video_download)
        spinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, qualities)
        defaultRange = context.getString(R.string.entire_video)
        //        tvRange.setText(defaultRange); //TODO change to start with "Download"
        //        rangeBar.setTickEnd(segments.size());
        //        rangeBar.setOnRangeBarChangeListener((rb, leftPinIndex, rightPinIndex, leftPinValue, rightPinValue) -> {
        //            String range;
        //            if (leftPinIndex != 0 && rightPinIndex != segments.size() - 1) {
        //                long from = 0;
        //                for (int i = 0; i < leftPinIndex; i++) {
        //                    from += segments.get(i).durationUs / 1000000;
        //                }
        //                long to = from;
        //                for (int i = leftPinIndex; i < rightPinIndex; i++) {
        //                    to += segments.get(i).durationUs / 1000000;
        //                }
        //                range = DateUtils.formatElapsedTime(from) + " - " + DateUtils.formatElapsedTime(to);
        //            } else {
        //                range = defaultRange;
        //            }
        //            tvRange.setText(getContext().getString(R.string.download_range, range));
        //        });
        cancel.setOnClickListener(this)
        download.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel -> dismiss()
            R.id.download -> {
                val keys = ArrayList<RenditionKey>()
                //                for (int i = rangeBar.getLeftIndex(); i < rangeBar.getRightIndex(); i++) {
                //                    keys.add(new RenditionKey(RenditionKey.TYPE_VARIANT, i));
                //                }
                listener.onClick(spinner.selectedItem.toString(), keys)
                dismiss()
            }
        }
    }
}
