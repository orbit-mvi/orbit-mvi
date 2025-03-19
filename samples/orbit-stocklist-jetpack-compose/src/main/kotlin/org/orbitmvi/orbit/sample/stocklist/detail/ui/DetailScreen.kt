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

package org.orbitmvi.orbit.sample.stocklist.detail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.sample.stocklist.common.ui.AppBar
import org.orbitmvi.orbit.sample.stocklist.common.ui.PriceBox
import org.orbitmvi.orbit.sample.stocklist.compose.R
import org.orbitmvi.orbit.sample.stocklist.detail.business.DetailViewModel

@Composable
@Suppress("LongMethod")
fun DetailScreen(navController: NavController, viewModel: DetailViewModel) {
    val state = viewModel.collectAsState().value

    Column {
        AppBar(state.stock?.name ?: stringResource(id = R.string.app_name)) {
            navController.popBackStack()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                PriceBox(
                    price = state.stock?.bid ?: "",
                    priceTick = state.stock?.bidTick,
                    color = colorResource(android.R.color.holo_red_dark),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
                PriceBox(
                    price = state.stock?.ask ?: "",
                    priceTick = state.stock?.askTick,
                    color = colorResource(android.R.color.holo_blue_dark),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
            ) {
                Text(
                    text = state.stock?.bidQuantity ?: "",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = state.stock?.askQuantity ?: "",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "Change %:",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = state.stock?.pctChange ?: "",
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Timestamp:",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = state.stock?.timestamp ?: "",
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "High:",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = state.stock?.max ?: "",
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Low:",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                Text(
                    text = state.stock?.min ?: "",
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}
