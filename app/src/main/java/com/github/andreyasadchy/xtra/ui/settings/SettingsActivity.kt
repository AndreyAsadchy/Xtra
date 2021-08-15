package com.github.andreyasadchy.xtra.ui.settings

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.Utils
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.DisplayUtils
import com.github.andreyasadchy.xtra.util.applyTheme
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        setContentView(R.layout.activity_settings)
        toolbar.navigationIcon = Utils.getNavigationIcon(this)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment())
                    .commit()
        }
    }

    override fun onPreferenceStartScreen(caller: PreferenceFragmentCompat, pref: PreferenceScreen): Boolean {
//        supportFragmentManager.beginTransaction()
//                .replace(R.id.settings, SettingsSubScreenFragment().apply { arguments = bundleOf(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT to pref.key) }, null)
//                .addToBackStack(null)
//                .commit()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private var changed = false

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            changed = savedInstanceState?.getBoolean(KEY_CHANGED) == true
            if (changed) {
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val activity = requireActivity()

            val changeListener = Preference.OnPreferenceChangeListener { _, _ ->
                setResult()
                true
            }
            findPreference<ListPreference>(C.PORTRAIT_COLUMN_COUNT)!!.onPreferenceChangeListener = changeListener
            findPreference<ListPreference>(C.LANDSCAPE_COLUMN_COUNT)!!.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.ANIMATED_EMOTES)!!.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.COMPACT_STREAMS)!!.onPreferenceChangeListener = changeListener
            findPreference<SwitchPreferenceCompat>(C.AD_BLOCKER)!!.onPreferenceChangeListener = changeListener
            findPreference<ListPreference>("playerForward")!!.onPreferenceChangeListener = changeListener
            findPreference<ListPreference>("playerRewind")!!.onPreferenceChangeListener = changeListener

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || !activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                findPreference<SwitchPreferenceCompat>(C.PICTURE_IN_PICTURE)!!.isVisible = false
            }

            findPreference<SwitchPreferenceCompat>(C.IGNORE_NOTCH)!!.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    onPreferenceChangeListener = changeListener
                } else {
                    isVisible = false
                }
            }

            findPreference<ListPreference>(C.THEME)!!.setOnPreferenceChangeListener { _, _ ->
                changed = true
                activity.apply {
                    applyTheme()
                    recreate()
                }
                true
            }

            findPreference<SeekBarPreference>("chatWidth")!!.setOnPreferenceChangeListener { _, newValue ->
                setResult()
                val chatWidth = DisplayUtils.calculateLandscapeWidthByPercent(activity, newValue as Int)
                activity.prefs().edit { putInt(C.LANDSCAPE_CHAT_WIDTH, chatWidth) }
                true
            }
        }

        override fun onSaveInstanceState(outState: Bundle) {
            outState.putBoolean(KEY_CHANGED, changed)
            super.onSaveInstanceState(outState)
        }

        private fun setResult() {
            if (!changed) {
                changed = true
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }

        companion object {
            const val KEY_CHANGED = "changed"
        }
    }

//    class SettingsSubScreenFragment : PreferenceFragmentCompat() {
//        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//            setPreferencesFromResource(R.xml.root_preferences, rootKey)
//        }
//    }
}