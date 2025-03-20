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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.features.postdetails.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.Res
import orbit_mvi.samples.orbit_posts_compose_multiplatform.composeapp.generated.resources.comments
import org.jetbrains.compose.resources.pluralStringResource
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.AppBar
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.Colors
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.common.elevation
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail.PostDetailState
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail.PostDetailsViewModel

@Composable
public fun PostDetailsScreen(navController: NavController, viewModel: PostDetailsViewModel) {

    val state = viewModel.collectAsState().value

    val lazyListState = rememberLazyListState()

    Column {
        AppBar(
            state.postOverview.username,
            iconPainter = rememberAsyncImagePainter(state.postOverview.avatarUrl),
            elevation = lazyListState.elevation
        ) {
            navController.popBackStack()
        }

        LazyColumn(state = lazyListState) {
            item {
                Text(
                    text = state.postOverview.title,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth()
                )
            }

            if (state is PostDetailState.Details) {
                item {
                    Text(
                        text = state.post.body,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )

                    Divider(color = Colors.separator, modifier = Modifier.padding(16.dp))

                    Text(
                        text = pluralStringResource(
                            Res.plurals.comments, state.post.comments.size, state.post.comments.size
                        ),
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }

                itemsIndexed(state.post.comments) { index, comment ->
                    if (index != 0) Divider(color = Colors.separator, modifier = Modifier.padding(horizontal = 16.dp))
                    PostCommentItem(comment)
                }
            }
        }
    }
}
