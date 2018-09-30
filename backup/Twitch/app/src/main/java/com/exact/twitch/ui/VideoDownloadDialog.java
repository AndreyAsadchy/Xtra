package com.exact.twitch.ui;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.appyvet.materialrangebar.RangeBar;
import com.exact.twitch.R;
import com.google.android.exoplayer2.source.hls.playlist.HlsMediaPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class VideoDownloadDialog extends Dialog implements View.OnClickListener {

    private Spinner spinner;
    private TextView tvRange;
    private RangeBar rangeBar;
    private Button btnCancel;
    private Button btnDownload;
    private final LinkedList<CharSequence> qualities;
    private final List<HlsMediaPlaylist.Segment> segments;
    private OnDownloadClickListener listener;
    private String defaultRange;

    public interface OnDownloadClickListener {
        void onClick(String quality, List<RenditionKey> keys);
    }

    public VideoDownloadDialog(Fragment fragment, LinkedList<CharSequence> qualities, List<HlsMediaPlaylist.Segment> segments) {
        super(fragment.requireActivity());
        this.qualities = qualities;
        this.segments = segments;
        listener = (OnDownloadClickListener) fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_video_download);
//        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinner = findViewById(R.id.dialog_video_download_spinner);
//        tvRange = findViewById(R.id.dialog_video_download_tv_range);
//        rangeBar = findViewById(R.id.dialog_video_download_rangebar);
        btnCancel = findViewById(R.id.dialog_video_download_btn_cancel);
        btnDownload = findViewById(R.id.dialog_video_download_btn_download);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, qualities);
        spinner.setAdapter(adapter);
//        defaultRange = getContext().getString(R.string.entire_video);
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
        btnCancel.setOnClickListener(this);
        btnDownload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_video_download_btn_cancel:
                dismiss();
                break;
            case R.id.dialog_video_download_btn_download:
                List<RenditionKey> keys = new ArrayList<>();
//                for (int i = rangeBar.getLeftIndex(); i < rangeBar.getRightIndex(); i++) {
//                    keys.add(new RenditionKey(RenditionKey.TYPE_VARIANT, i));
//                }
                listener.onClick(spinner.getSelectedItem().toString(), keys);
                dismiss();
                break;
        }
    }
}
