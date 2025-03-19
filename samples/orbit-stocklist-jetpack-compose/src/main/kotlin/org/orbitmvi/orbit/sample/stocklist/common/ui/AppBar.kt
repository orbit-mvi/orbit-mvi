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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import org.orbitmvi.orbit.sample.stocklist.compose.R

@Composable
fun AppBar(topAppBarText: String, onBackPressed: (() -> Unit)? = null) {
    TopAppBar(
        title = {
            Row {
                Image(
                    painterResource(id = R.drawable.ic_orbit_toolbar),
                    contentDescription = null
                )
                Text(
                    text = topAppBarText
                )
            }
        },
        navigationIcon = onBackPressed?.let {
            {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        backgroundColor = Color.White,
        contentColor = Color.Black
    )
}
