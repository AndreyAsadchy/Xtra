package com.github.exact7.xtra.ui.menu

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.model.User
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val isLoggedIn = User.get(activity) !is NotLoggedIn
        loginText.text = getString(if (isLoggedIn) R.string.log_out else R.string.log_in)
        search.setOnClickListener { activity.openSearch() }
        settings.setOnClickListener {
            activity.startActivityFromFragment(this, Intent(activity, SettingsActivity::class.java), 3)
        }
        rate.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${activity.packageName}")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${activity.packageName}")))
            }
        }
        donate.setOnClickListener { DonationDialog().show(childFragmentManager, null) }
        login.setOnClickListener {
            activity.startActivityForResult(Intent(activity, LoginActivity::class.java), if (!isLoggedIn) 1 else 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }
}