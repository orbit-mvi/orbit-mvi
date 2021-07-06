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

package org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel

import androidx.lifecycle.viewModelScope
import com.ww.roxie.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlow
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status

class PostDetailsViewModel(
    private val postRepository: PostRepository,
    private val postOverview: PostOverview
) : BaseViewModel<PostDetailsAction, PostDetailState>() {

    override val initialState: PostDetailState = PostDetailState.NoDetailsAvailable(postOverview)

    init {
        bindActions()
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun bindActions() {
        /* convert Actions into States here */
        viewModelScope.launch {
            actions.asFlow().mapLatest {
                postRepository.getDetail(postOverview.id)
            }.scan(initialState) { _, change ->
                when (change) {
                    is Status.Success -> PostDetailState.Details(postOverview, change.data)
                    is Status.Failure -> PostDetailState.NoDetailsAvailable(postOverview)
                }
            }.onEach {
                state.value = it
            }.launchIn(viewModelScope)
        }
    }
}
