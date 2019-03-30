package com.github.exact7.xtra.ui.pagers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R
import kotlinx.android.synthetic.main.fragment_media.*

abstract class MediaFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinner.setOnItemClickListener { _, _, position, _ ->
            childFragmentManager.beginTransaction().replace(R.id.container, onSpinnerItemSelected(position))
        }
    }

    abstract fun onSpinnerItemSelected(position: Int): Fragment
}