package com.github.exact7.xtra.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.github.exact7.xtra.R
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.fragment_media.*


abstract class MediaFragment : Fragment(), Scrollable {

    private var previousItem = -1
    private var currentFragment: Fragment? = null

    open val spinnerItems: Array<String>
        get() = resources.getStringArray(R.array.spinnerMedia)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        previousItem = savedInstanceState?.getInt("previousItem", -1) ?: -1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val activity = requireActivity() as MainActivity
        spinner.adapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item, spinnerItems)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentFragment = if (position != previousItem && isResumed) {
                    val newFragment = onSpinnerItemSelected(position)
                    childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, newFragment).commit()
                    previousItem = position
                    newFragment
                } else {
                    childFragmentManager.findFragmentById(R.id.fragmentContainer)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        search.setOnClickListener { activity.openSearch() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("previousItem", previousItem)
        super.onSaveInstanceState(outState)
    }

    abstract fun onSpinnerItemSelected(position: Int): Fragment

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        (currentFragment as? Scrollable)?.scrollToTop()
    }
}