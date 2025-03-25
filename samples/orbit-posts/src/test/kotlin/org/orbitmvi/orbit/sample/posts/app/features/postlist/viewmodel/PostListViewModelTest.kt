/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.orbitmvi.orbit.sample.posts.InstantTaskExecutorExtension
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.test.test

@ExtendWith(InstantTaskExecutorExtension::class)
class PostListViewModelTest {

    private val fixture = kotlinFixture()
    private val repository = mock<PostRepository>()

    @Test
    fun `loads post overviews from repository if no overviews present`() = runTest {
        val overviews = fixture<List<PostOverview>>()
        val initialState = PostListState()

        // given we mock the repository
        whenever(repository.getOverviews())
            .thenReturn(overviews)

        // when we observe details from the view model
        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            runOnCreate().join()

            // then the view model loads the overviews
            expectState { copy(overviews = overviews) }
        }
    }

    @Test
    fun `does not load post overviews from repository if already populated`() = runTest {
        val overviews = fixture<List<PostOverview>>()
        val initialState = PostListState(overviews)

        // given we mock the repository
        whenever(repository.getOverviews())
            .thenReturn(overviews)

        // when we observe details from the view model
        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            runOnCreate()

            expectNoItems()
        }
    }

    @Test
    fun `navigates to detail screen`() = runTest {
        val overviews = fixture<List<PostOverview>>()
        val detailTarget = overviews.random()
        val initialState = PostListState(overviews)

        // given we have already loaded the overviews
        PostListViewModel(SavedStateHandle(), repository).test(
            this,
            initialState = initialState,
        ) {
            // when we click a post
            containerHost.onPostClicked(detailTarget)

            // then we navigate to post details
            expectSideEffect(OpenPostNavigationEvent(detailTarget))
        }
    }
}
