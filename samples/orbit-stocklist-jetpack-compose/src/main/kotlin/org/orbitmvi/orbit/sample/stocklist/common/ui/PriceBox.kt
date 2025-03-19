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

package org.orbitmvi.orbit.sample.stocklist.common.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.orbitmvi.orbit.sample.stocklist.compose.R
import org.orbitmvi.orbit.sample.stocklist.streaming.stock.Tick

@Composable
fun PriceBox(
    price: String,
    priceTick: Tick?,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RectangleShape,
        elevation = 4.dp,
        color = color,
        modifier = modifier
            .padding(vertical = 4.dp)
            .padding(end = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            priceTick?.let {
                Icon(
                    painter = painterResource(id = if (priceTick == Tick.Up) R.drawable.arrow_up_bold else R.drawable.arrow_down_bold),
                    contentDescription = null,
                    tint = Color.White,
                )
            } ?: Spacer(Modifier)

            Text(
                price,
                style = style,
                color = Color.White,
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Preview
@Composable
fun PriceBoxPreview() {
    Column {
        PriceBox("2.64", Tick.Up, colorResource(android.R.color.holo_red_dark), MaterialTheme.typography.body2)

        PriceBox("2.63", Tick.Down, colorResource(android.R.color.holo_blue_dark), MaterialTheme.typography.body2)

        PriceBox("2.61", null, colorResource(android.R.color.holo_red_dark), MaterialTheme.typography.body2)
    }
}
