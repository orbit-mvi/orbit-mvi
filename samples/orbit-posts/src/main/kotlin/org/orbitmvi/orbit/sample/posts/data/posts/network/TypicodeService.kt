/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.orbitmvi.orbit.sample.posts.data.posts.network

import org.orbitmvi.orbit.sample.posts.data.posts.model.CommentData
import org.orbitmvi.orbit.sample.posts.data.posts.model.PostData
import org.orbitmvi.orbit.sample.posts.data.posts.model.UserData
import retrofit2.http.GET
import retrofit2.http.Path

// https://jsonplaceholder.typicode.com
interface TypicodeService {
    @GET("posts/{id}")
    suspend fun post(@Path("id") id: Int): PostData

    @GET("posts")
    suspend fun posts(): List<PostData>

    @GET("users/{id}")
    suspend fun user(@Path("id") id: Int): UserData

    @GET("users")
    suspend fun users(): List<UserData>

    @GET("comments")
    suspend fun comments(): List<CommentData>

    @GET("posts/{id}/comments")
    suspend fun comments(@Path("id") postId: Int): List<CommentData>
}
