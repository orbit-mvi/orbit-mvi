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
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostComment
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostRepository
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PostDetailsViewModelTest {

    private val overview = PostOverview.random()
    private val details = PostDetail.random().copy(id = overview.id)

    @Test
    fun `loads post details from repository if no details present`() = runTest {
        // Given an initial state with details loaded
        val initialState = PostDetailState.Loading(overview)
        // And a repository with detail
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = details
        }

        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState,
        ) {
            // When we run onCreate
            runOnCreate()

            // Then the view model loads the details
            expectState { PostDetailState.Ready(overview, details) }
        }
    }

    @Test
    fun `does not load post details from repository if details present`() = runTest {
        // Given an initial state with details loaded
        val initialState = PostDetailState.Ready(overview, details)
        // And a repository which errors on detail to prove it isn't called
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState
        ) {
            // When we run onCreate
            runOnCreate()

            // Then nothing happens
            expectNoItems()
        }
    }

    @Test
    fun `shows error when detail cannot load`() = runTest {
        // Given an initial state with loading
        val initialState = PostDetailState.Loading(overview)
        // And a repository which errors on detail
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState
        ) {
            // When we run onCreate
            runOnCreate()

            // Then the view model returns error
            val error = assertIs<PostDetailState.Error>(awaitState())
            assertEquals(overview, error.postOverview)
        }
    }

    @Test
    fun `retry on error loads overviews`() = runTest {
        // Given an initial state with an error
        val initialState = PostDetailState.Error(overview, onRetry = {})
        // And a repository with detail
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = details
        }

        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState
        ) {
            // And we run onCreate
            runOnCreate()
            // And capture the error
            val state = assertIs<PostDetailState.Error>(awaitState())

            // When we call onRetry
            state.onRetry()

            // Then the view model returns ready with the overviews
            expectState { PostDetailState.Ready(overview, details) }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun PostOverview.Companion.random() = PostOverview(
        id = Random.nextInt(),
        avatarUrl = Uuid.random().toString(),
        title = Uuid.random().toString(),
        username = Uuid.random().toString()
    )

    @OptIn(ExperimentalUuidApi::class)
    fun PostDetail.Companion.random() = PostDetail(
        id = Random.nextInt(),
        body = Uuid.random().toString(),
        comments = List(Random.nextInt(0, 5)) { PostComment.random() }
    )

    @OptIn(ExperimentalUuidApi::class)
    fun PostComment.Companion.random() = PostComment(
        id = Random.nextInt(),
        name = Uuid.random().toString(),
        email = Uuid.random().toString(),
        body = Uuid.random().toString()
    )
}
