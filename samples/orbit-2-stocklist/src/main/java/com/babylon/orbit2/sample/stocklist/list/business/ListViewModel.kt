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

package com.babylon.orbit2.sample.stocklist.list.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.coroutines.transformFlow
import com.babylon.orbit2.reduce
import com.babylon.orbit2.sample.stocklist.streaming.stock.StockRepository
import com.babylon.orbit2.sideEffect
import com.babylon.orbit2.viewmodel.container

class ListViewModel(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository
) : ViewModel(), ContainerHost<ListState, ListSideEffect> {

    override val container =
        container<ListState, ListSideEffect>(ListState(), savedStateHandle, onCreate = ::requestStocks, onRecreate = ::requestStocks)

    private fun requestStocks(): Unit = orbit {
        transformFlow {
            stockRepository.stockList()
        }.reduce {
            state.copy(stocks = event)
        }
    }

    fun viewMarket(itemName: String) = orbit {
        sideEffect {
            post(ListSideEffect.NavigateToDetail(itemName))
        }
    }
}
