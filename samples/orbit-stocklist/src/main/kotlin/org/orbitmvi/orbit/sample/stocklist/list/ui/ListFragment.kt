/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.orbitmvi.orbit.sample.stocklist.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import org.orbitmvi.orbit.sample.stocklist.R
import org.orbitmvi.orbit.sample.stocklist.databinding.ListFragmentBinding
import org.orbitmvi.orbit.sample.stocklist.list.business.ListSideEffect
import org.orbitmvi.orbit.sample.stocklist.list.business.ListViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

class ListFragment : Fragment() {

    private val listViewModel by viewModel<ListViewModel>()

    private lateinit var binding: ListFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val groupAdapter = GroupAdapter<GroupieViewHolder>()

        binding.recyclerView.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        lifecycleScope.launchWhenCreated {
            listViewModel.container.stateFlow.collect {
                val items = it.stocks.map { stock ->
                    StockItem(stock, listViewModel)
                }

                groupAdapter.update(items)
            }
        }
        lifecycleScope.launchWhenCreated {
            listViewModel.container.sideEffectFlow.collect {
                when (it) {
                    is ListSideEffect.NavigateToDetail ->
                        findNavController().navigate(ListFragmentDirections.actionListFragmentToDetailFragment(it.itemName))
                }
            }
        }
    }
}
