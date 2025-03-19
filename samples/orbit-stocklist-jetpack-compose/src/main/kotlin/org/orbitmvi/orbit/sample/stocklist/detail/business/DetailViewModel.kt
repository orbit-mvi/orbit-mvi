/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit.sample.stocklist.detail.business

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.StockRepository
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository
) : ViewModel(), ContainerHost<DetailState, Nothing> {

    private val itemName = savedStateHandle.get<String>("itemName")!!

    override val container = container<DetailState, Nothing>(DetailState(), savedStateHandle) { requestStock() }

    private fun requestStock() = intent(registerIdling = false) {
        repeatOnSubscription {
            stockRepository.stockDetails(itemName).collect {
                reduce {
                    state.copy(stock = it)
                }
            }
        }
    }
}
