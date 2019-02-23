package com.github.exact7.xtra.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.github.exact7.xtra.R
import com.github.exact7.xtra.util.C

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(C.THEME, true)) R.style.DarkTheme else R.style.LightTheme)
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
            preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                requireActivity().apply {
                    setTheme(if (newValue == true) R.style.DarkTheme else R.style.LightTheme)
                    recreate()
                }
                true
            }
        }
    }
}