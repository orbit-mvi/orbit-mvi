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
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.orbitmvi.orbit.sample.posts.InstantTaskExecutorExtension
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status
import org.orbitmvi.orbit.test.test
import java.io.IOException

@ExtendWith(InstantTaskExecutorExtension::class)
class PostDetailsViewModelTest {

    private val fixture = kotlinFixture()
    private val repository = mock<PostRepository>()

    @Test
    fun `loads post details from repository if no details present`() = runTest {
        val overview = fixture<PostOverview>()
        val details = fixture<PostDetail> {
            property(PostDetail::id) { overview.id }
        }
        val initialState = PostDetailState.NoDetailsAvailable(overview)

        // given we mock the repository
        whenever(repository.getDetail(overview.id))
            .thenReturn(Status.Success(details))

        // when we observe details from the view model
        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState,
        ) {
            expectInitialState()

            runOnCreate()

            // then the view model loads the details
            expectState { PostDetailState.Details(overview, details) }
        }
    }

    @Test
    fun `does not load post details from repository if details present`() = runTest {
        val overview = fixture<PostOverview>()
        val details = fixture<PostDetail> {
            property(PostDetail::id) { overview.id }
        }

        // given we already have details loaded
        val initialState = PostDetailState.Details(overview, details)

        // when we observe details from the view model
        // when we observe details from the view model
        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState,
        ) {
            runOnCreate()

            // then the view model only emits initial state
            expectState { initialState }

            expectNoItems()
        }
    }

    @Test
    fun `no details available on failure`() = runTest {
        val overview = fixture<PostOverview>()
        val exception = IOException()
        val initialState = PostDetailState.NoDetailsAvailable(overview)

        // given we mock the repository
        whenever(repository.getDetail(overview.id))
            .thenReturn(Status.Failure(exception))

        // when we observe details from the view model
        PostDetailsViewModel(SavedStateHandle(), repository, overview).test(
            this,
            initialState = initialState,
        ) {
            runOnCreate().join()

            // then the view model shows no details
            expectState { PostDetailState.NoDetailsAvailable(postOverview) }
        }
    }
}
