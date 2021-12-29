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

package org.orbitmvi.orbit.sample.posts.data.posts.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orbitmvi.orbit.sample.posts.data.posts.model.CommentData
import org.orbitmvi.orbit.sample.posts.data.posts.model.PostData
import org.orbitmvi.orbit.sample.posts.data.posts.model.UserData
import retrofit2.Retrofit
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import java.util.concurrent.TimeUnit

class PostNetworkDataSourceTest {
    private lateinit var behavior: NetworkBehavior
    private lateinit var api: TypicodeService

    @BeforeEach
    fun setup() {
        mockNetworkApi()
    }

    @Test
    fun `return posts when network ok`() {
        runBlocking {
            assertEquals(2, PostNetworkDataSource(api).getPosts().size)
        }
    }

    @Test
    fun `return empty posts when network errors`() {
        runBlocking {
            behavior.setErrorPercent(100)

            assertEquals(0, PostNetworkDataSource(api).getPosts().size)
        }
    }

    @Test
    fun `return empty posts when network fails`() {
        runBlocking {
            behavior.setFailurePercent(100)

            assertEquals(0, PostNetworkDataSource(api).getPosts().size)
        }
    }

    @Test
    fun `return users when network ok`() {
        runBlocking {
            assertEquals(2, PostNetworkDataSource(api).getUsers().size)
        }
    }

    @Test
    fun `return empty users when network errors`() {
        runBlocking {
            behavior.setErrorPercent(100)

            assertEquals(0, PostNetworkDataSource(api).getUsers().size)
        }
    }

    @Test
    fun `return empty users when network fails`() {
        runBlocking {
            behavior.setFailurePercent(100)

            assertEquals(0, PostNetworkDataSource(api).getUsers().size)
        }
    }

    @Test
    fun `return comments when network ok`() {
        runBlocking {
            assertEquals(2, PostNetworkDataSource(api).getComments().size)
        }
    }

    @Test
    fun `return empty comments when network errors`() {
        runBlocking {
            behavior.setErrorPercent(100)

            assertEquals(0, PostNetworkDataSource(api).getComments().size)
        }
    }

    @Test
    fun `return empty comments when network fails`() {
        runBlocking {
            behavior.setFailurePercent(100)

            assertEquals(0, PostNetworkDataSource(api).getComments().size)
        }
    }

    private fun mockNetworkApi() {
        behavior = NetworkBehavior.create().apply {
            setDelay(0, TimeUnit.MILLISECONDS)
            setVariancePercent(0)
            setErrorPercent(0)
            setFailurePercent(0)
        }

        val retrofit = Retrofit.Builder().baseUrl("http://mock.com").build()

        api = MockRetrofit.Builder(retrofit)
            .networkBehavior(behavior)
            .build()
            .create(TypicodeService::class.java)
            .let(PostNetworkDataSourceTest::MockApi)
    }

    class MockApi(private val delegate: BehaviorDelegate<TypicodeService>) :
        TypicodeService {
        private val posts = listOf(
            PostData(1, 1, "hi", "body"),
            PostData(2, 2, "hello", "body 2")
        )
        private val users = listOf(
            UserData(1, "bob", "username", "email"),
            UserData(2, "matt", "matt", "email")
        )
        private val comments = listOf(
            CommentData(1, 1, "content", "email", "body 3"),
            CommentData(
                2,
                1,
                "content 2",
                "email2",
                "body 4"
            )
        )

        override suspend fun post(id: Int): PostData {
            TODO("Not yet implemented")
        }

        override suspend fun posts(): List<PostData> {
            return delegate.returningResponse(posts).posts()
        }

        override suspend fun users(): List<UserData> {
            return delegate.returningResponse(users).users()
        }

        override suspend fun comments(): List<CommentData> {
            return delegate.returningResponse(comments).comments()
        }
    }
}
