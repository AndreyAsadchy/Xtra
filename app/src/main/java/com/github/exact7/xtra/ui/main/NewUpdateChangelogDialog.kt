package com.github.exact7.xtra.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.prefs
import kotlinx.android.synthetic.main.dialog_new_update_changes.*

class NewUpdateChangelogDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_new_update_changes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        changelog.text = ""
        ok.setOnClickListener {
            if (dontShow.isChecked) {
                requireContext().prefs().edit { putBoolean(C.SHOW_CHANGELOG, false) }
            }
            dismiss()
        }
    }
}