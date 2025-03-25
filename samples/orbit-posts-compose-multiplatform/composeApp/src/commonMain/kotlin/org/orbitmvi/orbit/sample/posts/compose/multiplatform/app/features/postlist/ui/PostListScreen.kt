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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.features.postlist.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.Res
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.Detail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.AppBar
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.Colors
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.elevation
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.NavigationEvent
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list.OpenPostNavigationEvent
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list.PostListViewModel

@Composable
public fun PostListScreen(navController: NavController, viewModel: PostListViewModel) {
    val state = viewModel.collectAsState().value

    LaunchedEffect(viewModel) {
        launch {
            viewModel.container.sideEffectFlow.collect { handleSideEffect(navController, it) }
        }
    }

    val lazyListState = rememberLazyListState()

    Column {
        AppBar(stringResource(Res.string.app_name), elevation = lazyListState.elevation)
        LazyColumn(state = lazyListState) {
            itemsIndexed(state.overviews) { index, post ->
                if (index != 0) Divider(color = Colors.separator, modifier = Modifier.padding(horizontal = 16.dp))

                PostListItem(post) {
                    viewModel.onPostClicked(it)
                }
            }
        }
    }
}

private fun handleSideEffect(navController: NavController, sideEffect: NavigationEvent) {
    when (sideEffect) {
        is OpenPostNavigationEvent -> {
            navController.navigate(Detail.of(sideEffect.post))
        }
    }
}
