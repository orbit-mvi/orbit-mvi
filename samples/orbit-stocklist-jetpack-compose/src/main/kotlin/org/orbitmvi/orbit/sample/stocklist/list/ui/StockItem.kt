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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.orbitmvi.orbit.sample.stocklist.common.ui.PriceBox
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.Stock
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.Tick

@Composable
fun StockItem(stock: Stock, onClick: (stock: Stock) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(stock) }
            .padding(8.dp)
    ) {
        Text(
            stock.name,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            PriceBox(
                price = stock.bid,
                priceTick = stock.bidTick,
                color = colorResource(android.R.color.holo_red_dark),
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            PriceBox(
                price = stock.ask,
                priceTick = stock.askTick,
                color = colorResource(android.R.color.holo_blue_dark),
                style = MaterialTheme.typography.body2,
                modifier = Modifier
                    .weight(1f)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
                    .padding(end = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    stock.timestamp,
                    style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun StocksPreview() {
    val stock = Stock("1", "Anduct", "2.64", Tick.Up, "2.65", Tick.Down, "06:35:08")
    StockItem(stock) {}
}

@Preview
@Composable
fun StocksPreview3() {
    val stock = Stock("1", "Anduct", "2.64", Tick.Down, "2.65", Tick.Up, "06:35:08")
    StockItem(stock) {}
}

@Preview
@Composable
fun StocksPreview2() {
    val stock = Stock("1", "Anduct", "2.64", null, "2.65", null, "06:35:08")
    StockItem(stock) {}
}
