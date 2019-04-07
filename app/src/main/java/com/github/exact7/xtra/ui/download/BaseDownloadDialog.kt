package com.github.exact7.xtra.ui.download

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.DownloadUtils
import com.github.exact7.xtra.util.Prefs
import kotlinx.android.synthetic.main.storage_selection.view.*

abstract class BaseDownloadDialog : DialogFragment(), Injectable {

    protected lateinit var prefs: SharedPreferences
    private lateinit var storageSelectionContainer: LinearLayout
    private lateinit var storage: List<Storage>
    protected val downloadPath: String
        get() {
            val index = if (storage.size == 1) {
                0
            } else {
                val checked = storageSelectionContainer.radioGroup.checkedRadioButtonId.let { if (it > -1) it else 0 }
                prefs.edit { putInt(C.DOWNLOAD_STORAGE, checked) }
                checked
            }
            return storage[index].path
        }

    fun init(context: Context) {
        prefs = Prefs.get(context)
        storage = DownloadUtils.getAvailableStorage(context)
        storageSelectionContainer = requireView().findViewById(R.id.storageSelectionContainer)
        if (DownloadUtils.isExternalStorageAvailable) {
            if (storage.size > 1) {
                storageSelectionContainer.visibility = View.VISIBLE
                for (s in storage) {
                    storageSelectionContainer.radioGroup.addView(RadioButton(context).apply {
                        id = s.id
                        text = s.name
                    })
                }
                storageSelectionContainer.radioGroup.check(prefs.getInt(C.DOWNLOAD_STORAGE, 0))
            }
        } else {
            storageSelectionContainer.visibility = View.VISIBLE
            storageSelectionContainer.noStorageDetected.visibility = View.VISIBLE
            requireView().findViewById<Button>(R.id.download).visibility = View.GONE
        }
    }

    data class Storage(
            val id: Int,
            val name: String,
            val path: String)
}