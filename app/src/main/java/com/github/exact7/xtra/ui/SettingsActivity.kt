package com.github.exact7.xtra.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.exact7.xtra.R
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.Prefs

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Prefs.userPrefs(this).getInt(C.THEME, R.style.DarkTheme))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val preference = findPreference<SwitchPreferenceCompat>("theme")
            val original = preference.isChecked
            preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val theme = if (newValue == true) {
                    R.style.DarkTheme
                } else {
                    R.style.LightTheme
                }
                requireActivity().apply {
                    setTheme(theme)
                    recreate()
                    setResult(Activity.RESULT_OK, Intent()
                            .putExtra("changed", newValue != original)
                            .putExtra("theme", theme))
                }
                true
            }
        }
    }
}