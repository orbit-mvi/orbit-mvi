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

package org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status
import org.reduxkotlin.ActionTypes
import org.reduxkotlin.Reducer
import org.reduxkotlin.Thunk
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createThreadSafeStore
import org.reduxkotlin.createThunkMiddleware
import org.reduxkotlin.middleware

class PostDetailsViewModel(
    postRepository: PostRepository,
    private val postOverview: PostOverview
) : ViewModel() {

    private val networkThunks = NetworkThunks(viewModelScope, postRepository)

    private val detailsMiddleware = middleware<PostDetailState> { store, next, action ->
        if (action is ActionTypes.INIT) {
            store.dispatch(networkThunks.loadDetails(postOverview.id))
        } else {
            next(action)
        }
    }

    private val detailsReducer: Reducer<PostDetailState> = { state, action ->
        when (action) {
            is PostDetailAction.DataSuccess -> PostDetailState.Details(state.postOverview, action.post)
            PostDetailAction.DataFailure -> PostDetailState.NoDetailsAvailable(state.postOverview)
            else -> state
        }
    }

    val store = createThreadSafeStore(
        reducer = detailsReducer,
        preloadedState = PostDetailState.NoDetailsAvailable(postOverview),
        enhancer = applyMiddleware(createThunkMiddleware(), detailsMiddleware)
    )

    init {
        store.dispatch(ActionTypes.INIT)
    }

    class NetworkThunks(private val coroutineScope: CoroutineScope, private val postRepository: PostRepository) {
        fun loadDetails(id: Int): Thunk<PostDetailState> = { dispatch, _, _ ->
            coroutineScope.launch {
                println("request")
                val status = postRepository.getDetail(id)
                println(status)
                when (status) {
                    is Status.Success -> dispatch(PostDetailAction.DataSuccess(status.data))
                    is Status.Failure -> dispatch(PostDetailAction.DataFailure)
                }
            }
        }
    }
}
