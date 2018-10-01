package com.exact.twitch.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.exact.twitch.R
import com.exact.twitch.ui.login.LoginActivity
import com.exact.twitch.ui.main.MainActivityViewModel
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        val mainActivityViewModel = ViewModelProviders.of(activity).get(MainActivityViewModel::class.java)
        login.text = if (mainActivityViewModel.isUserLoggedIn) "Log Out" else "Log In"
        login.setOnClickListener { startActivityForResult(Intent(activity, LoginActivity::class.java), 1) }
    }
}
