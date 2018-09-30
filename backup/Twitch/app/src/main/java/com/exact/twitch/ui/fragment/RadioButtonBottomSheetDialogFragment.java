package com.exact.twitch.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class RadioButtonBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnOptionSelectedListener {
        void onSelect(int optionId, CharSequence optionText);
    }

    private static final String BUTTONS = "buttons";
    private static final String SELECTED = "selected";

    private OnOptionSelectedListener listener;

    public static RadioButtonBottomSheetDialogFragment newInstance(ArrayList<RadioButton> buttons, int selectedItemId) {
        Bundle args = new Bundle();
        args.putSerializable(BUTTONS, buttons);
        args.putInt(SELECTED, selectedItemId);
        RadioButtonBottomSheetDialogFragment fragment = new RadioButtonBottomSheetDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        RadioGroup radioGroup = new RadioGroup(requireActivity()); //TODO add style
        @SuppressWarnings("unchecked") ArrayList<RadioButton> list = (ArrayList<RadioButton>) Objects.requireNonNull(getArguments()).getSerializable(BUTTONS);
        for (RadioButton button : list) {
            button.setOnClickListener(v -> {
                listener.onSelect(button.getId(), button.getText());
                dismiss();
            });
            radioGroup.addView(button, MATCH_PARENT, WRAP_CONTENT);
        }
        radioGroup.check(getArguments().getInt(SELECTED));
        return radioGroup;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment parentFragment = Objects.requireNonNull(getParentFragment());
        if (parentFragment instanceof OnOptionSelectedListener) {
            listener = (OnOptionSelectedListener) parentFragment;
        } else {
            throw new RuntimeException(parentFragment.toString() + " must implement RadioButtonBottomSheetDialogFragment.OnOptionSelectedListener");
        }
    }
}
