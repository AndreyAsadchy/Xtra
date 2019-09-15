package com.github.exact7.xtra.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.exact7.xtra.R
import kotlinx.android.synthetic.main.dialog_donation.*

class DonationDialog : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_donation, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this).get(DonationDialogViewModel::class.java)
        val activity = requireActivity()
        list.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, listOf("$0.99", "$2.49", "$3.49", "$4.99", "$7.49", "$9.99"))
        list.setOnItemClickListener { _, _, position, _ ->
            viewModel.launchBillingFlow(activity, position)
        }
        viewModel.state.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(activity, getString(R.string.thank_you_so_much), Toast.LENGTH_LONG).show()
            }
        })
    }
}