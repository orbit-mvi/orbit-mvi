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

import androidx.lifecycle.SavedStateHandle
import io.uniflow.android.AndroidDataFlow
import io.uniflow.core.flow.actionOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository

class PostListViewModel(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository
) : AndroidDataFlow(defaultState = PostListState(), savedStateHandle) {

    init {
        actionOn<PostListState> {
            if (it.overviews.isEmpty()) {
                loadOverviews()
            }
        }

        action1()
        action2()
    }

    private fun action1() = actionOn<PostListState> { state ->
        println("action1 - PostListState(action1=${state.action1}, action2=${state.action2})")

        withContext(Dispatchers.IO) {
            delay(5000)
        }

        setState {
            state.copy(action1 = true)
        }
    }

    private fun action2() = actionOn<PostListState> { state ->
        println("action2 - PostListState(action1=${state.action1}, action2=${state.action2})")
        withContext(Dispatchers.IO) {
            delay(2500)
        }

        setState {
            state.copy(action2 = true)
        }
    }

    private fun loadOverviews() = actionOn<PostListState> { state ->
        val overviews = postRepository.getOverviews()

        setState {
            state.copy(overviews = overviews)
        }
    }

    fun onPostClicked(post: PostOverview) = action {
        sendEvent(OpenPostNavigationEvent(post))
    }
}
