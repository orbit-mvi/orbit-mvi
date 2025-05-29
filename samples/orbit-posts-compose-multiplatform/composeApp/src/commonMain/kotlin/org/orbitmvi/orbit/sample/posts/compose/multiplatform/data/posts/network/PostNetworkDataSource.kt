/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts.model.CommentData
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts.model.PostData
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.data.posts.model.UserData

public class PostNetworkDataSource(private val client: HttpClient) {

    public suspend fun getPost(id: Int): PostData =
        client.getJson("posts/$id")

    public suspend fun getPosts(): List<PostData> =
        client.getJson<List<PostData>>("posts").sortedBy { it.title }

    public suspend fun getUsers(): List<UserData> =
        client.getJson("users")

    public suspend fun getUser(id: Int): UserData? =
        client.getJson<UserData>("users/$id")

    public suspend fun getComments(): List<CommentData> =
        client.getJson("comments")

    public suspend fun getComments(postId: Int): List<CommentData> =
        client.getJson("posts/$postId/comments")

    private suspend inline fun <reified T> HttpClient.getJson(urlString: String): T = get(urlString) {
        contentType(ContentType.Application.Json)
    }.body()
}
