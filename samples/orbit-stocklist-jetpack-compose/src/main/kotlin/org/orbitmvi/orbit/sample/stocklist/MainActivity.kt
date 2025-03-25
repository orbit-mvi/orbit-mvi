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

package org.orbitmvi.orbit.sample.stocklist

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.orbitmvi.orbit.sample.stocklist.detail.business.DetailViewModel
import org.orbitmvi.orbit.sample.stocklist.detail.ui.DetailScreen
import org.orbitmvi.orbit.sample.stocklist.list.business.ListViewModel
import org.orbitmvi.orbit.sample.stocklist.list.ui.ListScreen
import org.orbitmvi.orbit.sample.stocklist.streaming.StreamingClient
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var streamingClient: StreamingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(streamingClient)

        setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "list") {
                composable("list") {
                    val viewModel = hiltViewModel<ListViewModel>()
                    ListScreen(navController, viewModel)
                }
                composable("detail/{itemName}") {
                    val viewModel = hiltViewModel<DetailViewModel>()
                    DetailScreen(navController, viewModel)
                }
            }
        }
    }
}
