/*
 * Copyright 2022-2025 Mikołaj Leszczyński & Appmattus Limited
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
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.orbitmvi.orbit.compose.collectAsState

class MainActivity : AppCompatActivity() {

    private val viewModel = TextViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.collectAsState()
            val scaffoldState = rememberScaffoldState()

            Scaffold(
                scaffoldState = scaffoldState
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(16.dp)
                ) {
                    TextField(
                        label = { Text("Bad") },
                        value = state.badField,
                        onValueChange = { viewModel.updateTextBad(it) }
                    )

                    TextField(
                        label = { Text("Good") },
                        value = state.goodField,
                        onValueChange = { viewModel.updateTextGood(it) },
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.Gray)
                    )

                    Text("Demonstration of TextField state hoisting; validation occurs in the view model.")
                    TextField(
                        label = { Text(if (state.isTextFieldStateInError) "TextFieldState*" else "TextFieldState") },
                        state = state.textFieldState,
                        isError = state.isTextFieldStateInError,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Button(onClick = { viewModel.submit() }) { Text("Submit") }
                    Text(state.result)
                }
            }
        }
    }
}
