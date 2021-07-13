/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.orbitmvi.orbit.sample.posts.app.common.NavigationEvent
import org.orbitmvi.orbit.sample.posts.app.di.AssistedViewModelFactory
import org.orbitmvi.orbit.sample.posts.app.di.hiltMavericksViewModelFactory
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository

class PostListViewModel @AssistedInject constructor(
    @Assisted initialState: PostListState,
    private val postRepository: PostRepository
) : MavericksViewModel<PostListState>(initialState) {

    private val _sideEffect =
        Channel<NavigationEvent>(Channel.BUFFERED)
    val sideEffect: Flow<NavigationEvent> =
        _sideEffect.receiveAsFlow()

    init {
        loadOverviews()
    }

    private fun loadOverviews() = withState { state ->
        suspend {
            postRepository.getOverviews()
        }.execute { async ->
            async()?.let { copy(overviews = it) } ?: state
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPostClicked(post: PostOverview) {
        _sideEffect.sendBlocking(OpenPostNavigationEvent(post))
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<PostListViewModel, PostListState> {
        override fun create(state: PostListState): PostListViewModel
    }

    companion object : MavericksViewModelFactory<PostListViewModel, PostListState> by hiltMavericksViewModelFactory()
}
