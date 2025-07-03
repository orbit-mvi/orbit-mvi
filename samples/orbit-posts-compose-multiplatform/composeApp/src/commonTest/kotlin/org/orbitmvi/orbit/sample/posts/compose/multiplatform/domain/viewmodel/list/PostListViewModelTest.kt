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
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostRepository
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PostListViewModelTest {

    private val overviews = List(Random.nextInt(1, 5)) { PostOverview.random() }

    @Test
    fun `loads post overviews from repository if no overviews present`() = runTest {
        // Given an initial state with loading
        val initialState = PostListState.Loading
        // And a repository with overviews
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = overviews
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            // When we run onCreate
            runOnCreate()

            // Then the view model returns ready with the overviews
            expectState { PostListState.Ready(overviews = overviews) }
        }
    }

    @Test
    fun `shows error when overviews cannot load`() = runTest {
        // Given an initial state with loading
        val initialState = PostListState.Loading
        // And a repository which errors on overviews
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            // When we run onCreate
            runOnCreate()

            // Then the view model returns error
            assertIs<PostListState.Error>(awaitState())
        }
    }

    @Test
    fun `retry on error loads overviews`() = runTest {
        // Given an initial state with an error
        val initialState = PostListState.Error(onRetry = {})
        // And a repository with overviews
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = overviews
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState
        ) {
            // And we run onCreate
            runOnCreate()
            // And capture the error
            val state = assertIs<PostListState.Error>(awaitState())

            // When we call onRetry
            state.onRetry()

            // Then the view model returns ready with the overviews
            expectState { PostListState.Ready(overviews = overviews) }
        }
    }

    @Test
    fun `does not load post overviews from repository if already populated`() = runTest {
        // Given an initial state with overviews
        val initialState = PostListState.Ready(overviews)
        // And a repository which errors on overviews to prove it isn't called
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            // When we run onCreate
            runOnCreate()

            // Then nothing happens
            expectNoItems()
        }
    }

    @Test
    fun `navigates to detail screen`() = runTest {
        // Given an initial state with overviews
        val initialState = PostListState.Ready(overviews)
        // And a repository which errors on overviews
        val repository = object : PostRepository {
            override suspend fun getOverviews(): List<PostOverview> = error("Not implemented")
            override suspend fun getDetail(id: Int): PostDetail = error("Not implemented")
        }

        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            // When we click a random post
            val detailTarget = overviews.random()
            containerHost.onPostClicked(detailTarget)

            // Then we navigate to post details
            expectSideEffect(OpenPostNavigationEvent(detailTarget))
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun PostOverview.Companion.random() = PostOverview(
        id = Random.nextInt(),
        avatarUrl = Uuid.random().toString(),
        title = Uuid.random().toString(),
        username = Uuid.random().toString()
    )
}
