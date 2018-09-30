package com.exact.twitch.ui.videos;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.exact.twitch.R;

import java.util.Objects;

public class VideosSortDialog extends BottomSheetDialogFragment {

    private RadioGroup orderGroup;
    private RadioGroup periodGroup;
    private Button apply;

    private static final String ORDER = "order";
    private static final String PERIOD = "period";

    private OnSortOptionSelected optionListener;
    private View parent;

    public interface OnSortOptionSelected {
        void onSort(int orderItemId, CharSequence orderText, int periodItemId, CharSequence periodText);
    }

    public static VideosSortDialog newInstance(int orderItemId, int periodItemId) {
        Bundle args = new Bundle(2);
        args.putInt(ORDER, orderItemId);
        args.putInt(PERIOD, periodItemId);
        VideosSortDialog dialog = new VideosSortDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videos_sort_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        parent = view;
        orderGroup = view.findViewById(R.id.fragment_video_sort_dialog_order);
        periodGroup = view.findViewById(R.id.fragment_video_sort_dialog_period);
        apply = view.findViewById(R.id.fragment_video_sort_dialog_done);
        orderGroup.check(getArguments().getInt(ORDER));
        periodGroup.check(getArguments().getInt(PERIOD));

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        apply.setOnClickListener(v -> {
            RadioButton orderButton = parent.findViewById(orderGroup.getCheckedRadioButtonId());
            RadioButton periodButton = parent.findViewById(periodGroup.getCheckedRadioButtonId());
            optionListener.onSort(orderButton.getId(), orderButton.getText(), periodButton.getId(), periodButton.getText());
            dismiss();
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment parentFragment = Objects.requireNonNull(getParentFragment());
        if (parentFragment instanceof OnSortOptionSelected) {
            optionListener = (OnSortOptionSelected) parentFragment;
        } else {
            throw new RuntimeException(parentFragment.toString() + " must implement OnOptionSelectedListener");
        }
    }
}
