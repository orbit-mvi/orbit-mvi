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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.Status
import org.orbitmvi.orbit.viewmodel.container

public class PostDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val postOverview: PostOverview
) : ViewModel(), ContainerHost<PostDetailState, Nothing> {

    override val container: Container<PostDetailState, Nothing> =
        container(PostDetailState.NoDetailsAvailable(postOverview), savedStateHandle, PostDetailState.serializer()) {
            if (state !is PostDetailState.Details) {
                loadDetails()
            }
        }

    @OptIn(OrbitExperimental::class)
    private suspend fun loadDetails() = subIntent {
        val status = postRepository.getDetail(postOverview.id)

        reduce {
            when (status) {
                is Status.Success -> PostDetailState.Details(state.postOverview, status.data)
                is Status.Failure -> PostDetailState.NoDetailsAvailable(state.postOverview)
            }
        }
    }
}
