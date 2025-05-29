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

@file:Suppress("TooManyFunctions")

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.features.postdetails.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.Res
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.comments
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.detail_error_body
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.detail_error_retry
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.detail_error_title
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.ic_orbit_toolbar
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.loading_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.AppBar
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.Colors
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.elevation
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostComment
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail.PostDetailState
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail.PostDetailsViewModel
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
public fun PostDetailsScreen(navController: NavController, viewModel: PostDetailsViewModel) {
    val state = viewModel.collectAsState().value

    PostDetailsContent(state, onBack = { navController.popBackStack() })
}

@Composable
private fun PostDetailsContent(state: PostDetailState, onBack: () -> Unit) {
    val lazyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            AppBar(
                topAppBarText = state.postOverview.username,
                elevation = lazyListState.elevation,
                iconPainter = rememberAsyncImagePainter(state.postOverview.avatarUrl),
                onBackPressed = onBack
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(Modifier.padding(padding)) {
            when (val state = state) {
                is PostDetailState.Ready -> Ready(state.postOverview, state.post, lazyListState)
                is PostDetailState.Loading -> Loading(state.postOverview)
                is PostDetailState.Error -> Error(state.postOverview, state.onRetry)
            }
        }
    }
}

@Composable
private fun Ready(postOverview: PostOverview, post: PostDetail, lazyListState: LazyListState) {
    LazyColumn(state = lazyListState) {
        item {
            Text(
                text = postOverview.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                    .fillMaxWidth()
            )
        }

        item {
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )

            HorizontalDivider(color = Colors.separator, modifier = Modifier.padding(16.dp))

            Text(
                text = pluralStringResource(
                    Res.plurals.comments,
                    post.comments.size,
                    post.comments.size
                ),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }

        itemsIndexed(post.comments) { index, comment ->
            if (index != 0) HorizontalDivider(color = Colors.separator, modifier = Modifier.padding(horizontal = 16.dp))
            PostCommentItem(comment)
        }
    }
}

@Composable
private fun Loading(postOverview: PostOverview) {
    Text(
        text = postOverview.title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
    )

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
private fun Error(postOverview: PostOverview, onRetry: () -> Unit) {
    Text(
        text = postOverview.title,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
    )

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
            text = stringResource(Res.string.detail_error_title),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(Res.string.detail_error_body),
            modifier = Modifier.padding(horizontal = 32.dp),
            style = MaterialTheme.typography.titleMedium.copy(lineBreak = LineBreak.Paragraph),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(Res.string.detail_error_retry))
        }
    }
}

@Preview
@Composable
private fun ReadyPreview() {
    PostDetailsContent(PostDetailState.Ready(PostOverview.random(), PostDetail.random()), onBack = {})
}

@Preview
@Composable
private fun LoadingPreview() {
    PostDetailsContent(PostDetailState.Loading(PostOverview.random()), onBack = {})
}

@Preview
@Composable
private fun ErrorPreview() {
    PostDetailsContent(PostDetailState.Error(PostOverview.random(), {}), onBack = {})
}

@OptIn(ExperimentalUuidApi::class)
private fun PostOverview.Companion.random() = PostOverview(
    id = Random.nextInt(),
    avatarUrl = Uuid.random().toString(),
    title = Uuid.random().toString(),
    username = Uuid.random().toString()
)

@OptIn(ExperimentalUuidApi::class)
private fun PostDetail.Companion.random() = PostDetail(
    id = Random.nextInt(),
    body = Uuid.random().toString(),
    comments = List(Random.nextInt(0, PREVIEW_LIST_COUNT)) { PostComment.random() }
)

@OptIn(ExperimentalUuidApi::class)
private fun PostComment.Companion.random() = PostComment(
    id = Random.nextInt(),
    name = Uuid.random().toString(),
    email = Uuid.random().toString(),
    body = Uuid.random().toString()
)

private const val PREVIEW_LIST_COUNT = 5
