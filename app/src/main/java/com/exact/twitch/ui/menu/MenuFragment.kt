package com.exact.twitch.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.exact.twitch.R
import com.exact.twitch.util.TwitchApiHelper
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.text = if (TwitchApiHelper.getUserToken(requireActivity()) == null) "Log In" else  "Log Out"
//        login.setOnClickListener { findNavController().navigate(R.id.activity_login) }
    }
}
