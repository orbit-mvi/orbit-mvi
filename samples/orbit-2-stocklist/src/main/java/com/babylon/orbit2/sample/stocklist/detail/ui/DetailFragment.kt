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

package com.babylon.orbit2.sample.stocklist.detail.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.babylon.orbit2.livedata.state
import com.babylon.orbit2.sample.stocklist.R
import com.babylon.orbit2.sample.stocklist.databinding.DetailFragmentBinding
import com.babylon.orbit2.sample.stocklist.detail.business.DetailViewModel
import com.babylon.orbit2.sample.stocklist.list.ui.JobHolder
import com.babylon.orbit2.sample.stocklist.list.ui.animateChange
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.core.parameter.parametersOf

class DetailFragment : Fragment() {

    private val args: DetailFragmentArgs by navArgs()
    private val detailViewModel by stateViewModel<DetailViewModel> { parametersOf(args.itemName) }
    private lateinit var binding: DetailFragmentBinding

    private val bidRef = JobHolder()
    private val askRef = JobHolder()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.detail_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.apply {
            state = detailViewModel.container.state
            lifecycleOwner = this@DetailFragment
        }

        detailViewModel.container.state.observe(viewLifecycleOwner, Observer {
            it.stock?.let { stock ->
                animateChange(binding.bid, binding.bidTick, stock.bid, bidRef)
                animateChange(binding.ask, binding.askTick, stock.ask, askRef)
            }
        })
    }
}
