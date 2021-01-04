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

package org.orbitmvi.orbit.sample.posts.data.posts

import org.orbitmvi.orbit.sample.posts.data.posts.network.AvatarUrlGenerator
import org.orbitmvi.orbit.sample.posts.data.posts.network.PostNetworkDataSource
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostComment
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PostDataRepository(
    private val networkDataSource: PostNetworkDataSource,
    private val avatarUrlGenerator: AvatarUrlGenerator
) : PostRepository {
    override suspend fun getOverviews(): List<PostOverview> {
        return coroutineScope {
            val posts = async { networkDataSource.getPosts() }
            val users = async { networkDataSource.getUsers() }

            posts.await().map { post ->
                val user = users.await().first { it.id == post.userId }

                PostOverview(
                    post.id,
                    avatarUrlGenerator.generateUrl(user.email),
                    post.title,
                    user.name
                )
            }
        }
    }

    override suspend fun getDetail(id: Int): Status<PostDetail> {
        return coroutineScope {
            when (val postData = networkDataSource.getPost(id)) {
                is Status.Success -> {
                    val comments = async {
                        networkDataSource.getComments()
                            .filter { it.postId == postData.data.id }
                    }

                    Status.Success(
                        PostDetail(
                            postData.data.id,
                            postData.data.body,
                            comments.await().map {
                                PostComment(
                                    it.id,
                                    it.name,
                                    it.email,
                                    it.body
                                )
                            }
                        )
                    )
                }
                is Status.Failure -> Status.Failure<PostDetail>(postData.exception)
            }
        }
    }
}
