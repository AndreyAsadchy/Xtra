package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R
import kotlinx.android.synthetic.main.fragment_media.*

abstract class MediaFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    protected fun initSpinner() {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                childFragmentManager.beginTransaction().replace(R.id.container, onSpinnerItemSelected(position)).commit()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    abstract fun onSpinnerItemSelected(position: Int): Fragment
}