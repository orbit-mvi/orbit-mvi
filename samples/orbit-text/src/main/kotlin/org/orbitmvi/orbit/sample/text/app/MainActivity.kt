/*
 * Copyright 2022-2023 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.sample.text.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import org.orbitmvi.orbit.compose.collectAsState

class MainActivity : ComponentActivity() {

    private val viewModel = TextViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            ComposeView(this).apply {
                setContent {
                    val state by viewModel.collectAsState()

                    Column {
                        TextField(
                            label = { Text("Bad") },
                            value = state.badField,
                            onValueChange = { viewModel.updateTextBad(it) }
                        )

                        TextField(
                            label = { Text("Good") },
                            value = state.goodField,
                            onValueChange = { viewModel.updateTextGood(it) }
                        )
                    }
                }
            }
        )
    }
}
