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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container

class PostDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val postOverview: PostOverview
) : ViewModel(), ContainerHost<PostDetailState, Nothing> {

    override var container = container<PostDetailState, Nothing>(PostDetailState.NoDetailsAvailable(postOverview), savedStateHandle) {
        if (it !is PostDetailState.Details) {
            loadDetails()
        }
    }

    private fun loadDetails() = intent {
        val status = postRepository.getDetail(postOverview.id)

        reduce {
            when (status) {
                is Status.Success -> PostDetailState.Details(state.postOverview, status.data)
                is Status.Failure -> PostDetailState.NoDetailsAvailable(state.postOverview)
            }
        }
    }
}
