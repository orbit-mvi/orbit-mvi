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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.features.postlist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.Res
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.app_name
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.ic_orbit_toolbar
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.loading_title
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.overview_error_body
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.overview_error_retry
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.overview_error_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.Detail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.AppBar
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.Colors
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.elevation
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.NavigationEvent
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list.OpenPostNavigationEvent
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list.PostListState
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list.PostListViewModel
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
public fun PostListScreen(navController: NavController, viewModel: PostListViewModel) {
    val state by viewModel.collectAsState()

    viewModel.collectSideEffect { handleSideEffect(navController, it) }

    PostListContent(state, viewModel::onPostClicked)
}

@Composable
private fun PostListContent(state: PostListState, onPostClicked: (PostOverview) -> Unit) {
    val lazyListState = rememberLazyListState()

    Scaffold(
        topBar = { AppBar(stringResource(Res.string.app_name), elevation = lazyListState.elevation) },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
        ) {
            when (val state = state) {
                is PostListState.Ready -> Ready(state.overviews, lazyListState, onPostClicked = onPostClicked)
                is PostListState.Loading -> Loading()
                is PostListState.Error -> Error(state.onRetry)
            }
        }
    }
}

@Composable
private fun Ready(overviews: List<PostOverview>, lazyListState: LazyListState, onPostClicked: (PostOverview) -> Unit) {
    LazyColumn(state = lazyListState) {
        itemsIndexed(overviews) { index, post ->
            if (index != 0) HorizontalDivider(color = Colors.separator, modifier = Modifier.padding(horizontal = 16.dp))

            PostListItem(post) { onPostClicked(it) }
        }
    }
}

@Preview
@Composable
private fun ReadyPreview() {
    val overviews = List(PREVIEW_LIST_COUNT) { PostOverview.random() }
    PostListContent(PostListState.Ready(overviews), onPostClicked = {})
}

@Preview
@Composable
private fun LoadingPreview() {
    PostListContent(PostListState.Loading, onPostClicked = {})
}

@Preview
@Composable
private fun ErrorPreview() {
    PostListContent(PostListState.Error({}), onPostClicked = {})
}

@OptIn(ExperimentalUuidApi::class)
private fun PostOverview.Companion.random() = PostOverview(
    id = Random.nextInt(),
    avatarUrl = Uuid.random().toString(),
    title = Uuid.random().toString(),
    username = Uuid.random().toString()
)

@Composable
private fun Loading() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.ic_orbit_toolbar),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.loading_title),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        CircularProgressIndicator()
    }
}

@Composable
private fun Error(onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.ic_orbit_toolbar),
            contentDescription = null,
            modifier = Modifier.size(128.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.overview_error_title),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.overview_error_body),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Paragraph),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(Res.string.overview_error_retry))
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

private const val PREVIEW_LIST_COUNT = 5
