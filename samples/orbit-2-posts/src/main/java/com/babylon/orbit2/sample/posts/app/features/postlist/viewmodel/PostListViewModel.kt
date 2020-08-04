/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.sample.posts.app.features.postlist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.coroutines.transformSuspend
import com.babylon.orbit2.reduce
import com.babylon.orbit2.sample.posts.app.common.NavigationEvent
import com.babylon.orbit2.sample.posts.domain.repositories.PostOverview
import com.babylon.orbit2.sample.posts.domain.repositories.PostRepository
import com.babylon.orbit2.sideEffect
import com.babylon.orbit2.viewmodel.container

class PostListViewModel(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository
) : ViewModel(), ContainerHost<PostListState, NavigationEvent> {

    override val container = container<PostListState, NavigationEvent>(PostListState(), savedStateHandle) {
        if (it.overviews.isEmpty()) {
            loadOverviews()
        }
    }

    private fun loadOverviews() = orbit {
        transformSuspend {
            postRepository.getOverviews()
        }.reduce {
            state.copy(overviews = event)
        }
    }

    fun onPostClicked(post: PostOverview) = orbit {
        sideEffect {
            post(OpenPostNavigationEvent(post))
        }
    }
}
