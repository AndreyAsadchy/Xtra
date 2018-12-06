package com.github.exact7.xtra.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.login.LoginActivity
import com.github.exact7.xtra.ui.main.MainViewModel
import kotlinx.android.synthetic.main.fragment_menu.*

class MenuFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity()
        val viewModel = ViewModelProviders.of(activity).get(MainViewModel::class.java)
        viewModel.user.observe(this, Observer { login.text = if (it != null) getString(R.string.log_out) else getString(R.string.log_in) })
        login.setOnClickListener {
            activity.startActivityForResult(
                    Intent(activity, LoginActivity::class.java),
                    if (viewModel.user.value == null) 1 else 2
            )
        }
    }
}
