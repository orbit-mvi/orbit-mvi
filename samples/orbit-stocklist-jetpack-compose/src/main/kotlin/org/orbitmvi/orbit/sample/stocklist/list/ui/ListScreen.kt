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

package org.orbitmvi.orbit.sample.stocklist.list.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import org.orbitmvi.orbit.sample.stocklist.common.ui.AppBar
import org.orbitmvi.orbit.sample.stocklist.compose.R
import org.orbitmvi.orbit.sample.stocklist.list.business.ListSideEffect
import org.orbitmvi.orbit.sample.stocklist.list.business.ListViewModel

@Composable
fun ListScreen(navController: NavController, viewModel: ListViewModel) {
    val state = viewModel.collectAsState()
    viewModel.collectSideEffect { handleSideEffect(navController, it) }

    Column {
        AppBar(stringResource(id = R.string.app_name))

        LazyColumn {
            items(state.value.stocks) { stock ->
                StockItem(stock) {
                    viewModel.viewMarket(stock.itemName)
                }
            }
        }
    }
}

private fun handleSideEffect(navController: NavController, sideEffect: ListSideEffect) {
    when (sideEffect) {
        is ListSideEffect.NavigateToDetail -> navController.navigate("detail/${sideEffect.itemName}")
    }
}
