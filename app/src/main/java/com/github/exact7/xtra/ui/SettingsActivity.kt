package com.github.exact7.xtra.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.github.exact7.xtra.R
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(C.THEME, true)) R.style.DarkTheme else R.style.LightTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val activity = requireActivity()
            findPreference<SwitchPreferenceCompat>("theme").setOnPreferenceChangeListener { _, newValue ->
                activity.apply {
                    setTheme(if (newValue == true) R.style.DarkTheme else R.style.LightTheme)
                    recreate()
                }
                true
            }
            findPreference<EditTextPreference>("chatWidth").setOnPreferenceChangeListener { _, newValue ->
                val value = newValue.toString().let { if (it.isNotEmpty()) it.toInt() else return@setOnPreferenceChangeListener false }
                return@setOnPreferenceChangeListener if (value in 15..50) {
                    val displayMetrics = DisplayMetrics()
                    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                    val deviceLandscapeWidth = with(displayMetrics) {
                        if (heightPixels > widthPixels) heightPixels else widthPixels
                    }
                    val chatWidth = (deviceLandscapeWidth * (value / 100f)).toInt()
                    PreferenceManager.getDefaultSharedPreferences(context).edit { putInt(C.LANDSCAPE_CHAT_WIDTH, chatWidth) }
                    activity.setResult(Activity.RESULT_OK, Intent().putExtra(C.LANDSCAPE_CHAT_WIDTH, chatWidth))
                    true
                } else {
                    Toast.makeText(context, getString(R.string.landscape_chat_width_error), Toast.LENGTH_LONG).show()
                    false
                }
            }
        }
    }
}