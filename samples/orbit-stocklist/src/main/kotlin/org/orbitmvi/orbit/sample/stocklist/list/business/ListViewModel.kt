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

package org.orbitmvi.orbit.sample.stocklist.list.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.coroutines.transformFlow
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.StockRepository
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.syntax.strict.reduce
import org.orbitmvi.orbit.syntax.strict.sideEffect
import org.orbitmvi.orbit.viewmodel.container

class ListViewModel(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository
) : ViewModel(), ContainerHost<ListState, ListSideEffect> {

    override val container = container<ListState, ListSideEffect>(ListState(), savedStateHandle) { requestStocks() }

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
