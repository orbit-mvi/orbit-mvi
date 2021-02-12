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

package org.orbitmvi.orbit.sample.stocklist.detail.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.coroutines.transformFlow
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.StockRepository
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.viewmodel.container

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemName: String,
    private val stockRepository: StockRepository
) : ViewModel(), ContainerHost<DetailState, Nothing> {

    override val container =
        container<DetailState, Nothing>(DetailState(), savedStateHandle) { requestStock() }

    private fun requestStock(): Unit = orbit {
        transformFlow {
            stockRepository.stockDetails(itemName)
        }.reduce {
            state.copy(stock = event)
        }
    }
}
