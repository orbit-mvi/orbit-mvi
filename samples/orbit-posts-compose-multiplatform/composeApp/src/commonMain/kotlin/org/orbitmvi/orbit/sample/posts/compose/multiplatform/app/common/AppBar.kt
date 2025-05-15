/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.Res
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.ic_orbit_toolbar
import org.jetbrains.compose.resources.painterResource

@Composable
public fun AppBar(
    topAppBarText: String,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    iconPainter: Painter? = null,
    onBackPressed: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row {
                Image(
                    iconPainter ?: painterResource(Res.drawable.ic_orbit_toolbar),
                    contentDescription = null,
                    modifier = Modifier.height(24.dp)
                )
                Text(
                    text = topAppBarText,
                    modifier = Modifier.padding(start = 24.dp)
                )
            }
        },
        navigationIcon = onBackPressed?.let {
            {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = elevation
    )
}
