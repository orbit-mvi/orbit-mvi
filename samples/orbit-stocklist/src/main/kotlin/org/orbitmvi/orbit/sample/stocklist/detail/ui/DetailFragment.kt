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

package org.orbitmvi.orbit.sample.stocklist.detail.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.sample.stocklist.databinding.DetailFragmentBinding
import org.orbitmvi.orbit.sample.stocklist.detail.business.DetailState
import org.orbitmvi.orbit.sample.stocklist.detail.business.DetailViewModel
import org.orbitmvi.orbit.sample.stocklist.list.ui.JobHolder
import org.orbitmvi.orbit.sample.stocklist.list.ui.animateChange
import org.orbitmvi.orbit.viewmodel.observe

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()
    private val detailViewModel by viewModel<DetailViewModel> { parametersOf(args.itemName) }

    private val bidRef = JobHolder()
    private val askRef = JobHolder()

    private var _binding: DetailFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailFragmentBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            state = detailViewModel.container.stateFlow.asLiveData()
            lifecycleOwner = this@DetailFragment
        }

        detailViewModel.observe(viewLifecycleOwner, state = ::render)
    }

    fun render(state: DetailState) {
        state.stock?.let { stock ->
            binding.change.text = stock.pctChange
            binding.max.text = stock.max
            binding.low.text = stock.min
            animateChange(binding.bid, binding.bidTick, stock.bid, bidRef)
            animateChange(binding.ask, binding.askTick, stock.ask, askRef)
        }
    }
}
