package com.github.exact7.xtra.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.exact7.xtra.databinding.FragmentSearchBinding
import com.github.exact7.xtra.di.Injectable
import com.github.exact7.xtra.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_search.*
import javax.inject.Inject

class SearchFragment : Fragment(), Injectable {

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            FragmentSearchBinding.inflate(inflater, container, false).let {
                binding = it
                it.lifecycleOwner = viewLifecycleOwner
                binding.root
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)
        binding.viewModel = viewModel
        val activity = requireActivity() as MainActivity
        val adapter = ChannelsSearchAdapter(activity)
        recyclerView.adapter = adapter
        val search = activity.search
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(if (viewModel.query.isNotEmpty()) it else null)
        })
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (adapter.currentList?.isEmpty() != false) {
                    viewModel.query = query
                }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (viewModel.query != newText) {
                    adapter.submitList(null)
                    viewModel.query = newText
                }
                return false
            }
        })
    }
}