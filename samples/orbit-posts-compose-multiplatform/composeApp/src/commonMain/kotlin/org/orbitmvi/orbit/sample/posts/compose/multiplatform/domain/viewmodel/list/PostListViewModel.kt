/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Job
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.NavigationEvent
import org.orbitmvi.orbit.viewmodel.container

public class PostListViewModel(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository
) : ViewModel(), ContainerHost<PostListState, NavigationEvent> {

    override val container: Container<PostListState, NavigationEvent> = container(
        initialState = PostListState.Loading,
        savedStateHandle = savedStateHandle,
        serializer = PostListState.serializer()
    ) {
        when (state) {
            is PostListState.Error -> reduce { PostListState.Error(::onRetry) }
            is PostListState.Loading -> loadOverviews()
            is PostListState.Ready -> Unit
        }
    }

    @OptIn(OrbitExperimental::class)
    private suspend fun loadOverviews() = subIntent {
        runCatching {
            postRepository.getOverviews()
        }.onSuccess { overviews ->
            reduce { PostListState.Ready(overviews) }
        }.onFailure {
            reduce { PostListState.Error(::onRetry) }
        }
    }

    private fun onRetry(): Job = intent {
        loadOverviews()
    }

    public fun onPostClicked(post: PostOverview): Job = intent {
        postSideEffect(OpenPostNavigationEvent(post))
    }
}
