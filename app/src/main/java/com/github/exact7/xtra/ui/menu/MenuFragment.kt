package com.github.exact7.xtra.ui.menu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.model.NotLoggedIn
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import com.github.exact7.xtra.ui.settings.SettingsActivity
import com.github.exact7.xtra.util.C
import com.github.exact7.xtra.util.applyTheme
import com.github.exact7.xtra.util.isInLandscapeOrientation
import com.github.exact7.xtra.util.prefs
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        val viewModel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        viewModel.user.observe(this, Observer { loginText.text = if (it !is NotLoggedIn) getString(R.string.log_out) else getString(R.string.log_in) })
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
            activity.startActivityForResult(Intent(activity, LoginActivity::class.java), if (viewModel.user.value is NotLoggedIn) 1 else 2)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val activity = requireActivity() as MainActivity
        if (activity.currentTheme != activity.prefs().getString(C.THEME, "0")) {
            activity.apply {
                applyTheme()
                recreate()
            }
        }
        data?.let {
            it.getIntExtra(C.LANDSCAPE_CHAT_WIDTH, -1).let { value ->
                if (value > -1 && activity.isInLandscapeOrientation) {
                    activity.playerContainer?.findViewById<FrameLayout>(R.id.chatFragmentContainer)?.updateLayoutParams { width = value }
                }
            }
            if (it.getBooleanExtra("shouldRecreate", false) || it.getBooleanExtra("changedAnimatedEmotes", false)) {
                activity.recreate()
            }
        }
    }
}