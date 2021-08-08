package com.github.andreyasadchy.xtra.ui.download

import android.content.Context
import android.content.SharedPreferences
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.content.edit
import androidx.fragment.app.DialogFragment
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.DownloadUtils
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.visible
import kotlinx.android.synthetic.main.storage_selection.view.*
import kotlin.math.max

abstract class BaseDownloadDialog : DialogFragment(), Injectable {

    protected lateinit var prefs: SharedPreferences
    private lateinit var storageSelectionContainer: LinearLayout
    private lateinit var storage: List<Storage>
    protected val downloadPath: String
        get() {
            val index = if (storage.size == 1) {
                0
            } else {
                val checked = max(storageSelectionContainer.radioGroup.checkedRadioButtonId, 0)
                prefs.edit { putInt(C.DOWNLOAD_STORAGE, checked) }
                checked
            }
            return storage[index].path
        }

    protected fun init(context: Context) {
        storageSelectionContainer = requireView().findViewById(R.id.storageSelectionContainer)
        prefs = context.prefs()
        storage = DownloadUtils.getAvailableStorage(context)
        if (DownloadUtils.isExternalStorageAvailable) {
            if (storage.size > 1) {
                storageSelectionContainer.visible()
                for (s in storage) {
                    storageSelectionContainer.radioGroup.addView(RadioButton(context).apply {
                        id = s.id
                        text = s.name
                    })
                }
                storageSelectionContainer.radioGroup.check(prefs.getInt(C.DOWNLOAD_STORAGE, 0))
            }
        } else {
            storageSelectionContainer.visible()
            storageSelectionContainer.noStorageDetected.visible()
            requireView().findViewById<Button>(R.id.download).gone()
        }
    }

    data class Storage(
            val id: Int,
            val name: String,
            val path: String)
}