/*
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

package com.babylon.orbit2.sample.posts.data.posts.network

import com.babylon.orbit2.sample.posts.data.posts.common.model.CommentData
import com.babylon.orbit2.sample.posts.data.posts.common.model.PostData
import com.babylon.orbit2.sample.posts.data.posts.common.model.UserData
import com.babylon.orbit2.sample.posts.domain.repositories.Status
import retrofit2.HttpException
import java.io.IOException

class PostNetworkDataSource(private val typicodeService: TypicodeService) {
    suspend fun getPost(id: Int): Status<PostData> {
        return try {
            Status.Success(typicodeService.post(id))
        } catch (e: IOException) {
            Status.Failure(e)
        } catch (e: HttpException) {
            Status.Failure(e)
        }
    }

    suspend fun getPosts(): List<PostData> {
        return try {
            typicodeService.posts()
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun getUsers(): List<UserData> {
        return try {
            typicodeService.users()
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun getUser(id: Int): UserData? {
        return try {
            typicodeService.user(id)
        } catch (e: IOException) {
            null
        } catch (e: HttpException) {
            null
        }
    }

    suspend fun getComments(): List<CommentData> {
        return try {
            typicodeService.comments()
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun getComments(postId: Int): List<CommentData> {
        return try {
            typicodeService.comments(postId)
        } catch (e: IOException) {
            emptyList()
        } catch (e: HttpException) {
            emptyList()
        }
    }
}
