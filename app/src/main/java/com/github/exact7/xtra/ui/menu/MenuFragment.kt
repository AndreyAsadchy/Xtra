package com.github.exact7.xtra.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.ui.SettingsActivity
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.util.C
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        val viewModel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        viewModel.user.observe(this, Observer { loginText.text = if (it !is NotLoggedIn) getString(R.string.log_out) else getString(R.string.log_in) })
        settings.setOnClickListener {
            activity.startActivityFromFragment(this, Intent(activity, SettingsActivity::class.java), 3)
        }
        login.setOnClickListener {
            activity.startActivityForResult(Intent(activity, LoginActivity::class.java), if (viewModel.user.value is NotLoggedIn) 1 else 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val activity = requireActivity() as MainActivity
        val darkTheme = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(C.THEME, true)
        if (activity.isDarkTheme != darkTheme) {
            activity.apply {
                setTheme(if (darkTheme) R.style.DarkTheme else R.style.LightTheme)
                recreate()
            }
        }
    }
}
