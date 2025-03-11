/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.sample.stocklist.list.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.orbitmvi.orbit.sample.stocklist.R
import org.orbitmvi.orbit.sample.stocklist.databinding.ListFragmentBinding
import org.orbitmvi.orbit.sample.stocklist.list.business.ListSideEffect
import org.orbitmvi.orbit.sample.stocklist.list.business.ListState
import org.orbitmvi.orbit.sample.stocklist.list.business.ListViewModel
import org.orbitmvi.orbit.viewmodel.observe

class ListFragment : Fragment() {

    private val listViewModel by viewModel<ListViewModel>()

    private lateinit var binding: ListFragmentBinding

    private val groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.list_fragment, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            itemAnimator = null
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        listViewModel.observe(viewLifecycleOwner, state = ::render, sideEffect = ::sideEffect)
    }

    fun render(state: ListState) {
        val items = state.stocks.map { stock ->
            StockItem(stock, listViewModel)
        }

        groupAdapter.update(items)
    }

    fun sideEffect(sideEffect: ListSideEffect): Unit = when (sideEffect) {
        is ListSideEffect.NavigateToDetail ->
            findNavController().navigate(ListFragmentDirections.actionListFragmentToDetailFragment(sideEffect.itemName))
    }
}
