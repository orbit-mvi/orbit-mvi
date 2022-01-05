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

import org.orbitmvi.orbit.sample.posts.data.posts.model.CommentData
import org.orbitmvi.orbit.sample.posts.data.posts.model.PostData
import org.orbitmvi.orbit.sample.posts.data.posts.model.UserData
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status
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
            typicodeService.posts().sortedBy { it.title }
        } catch (ignore: IOException) {
            emptyList()
        } catch (ignore: HttpException) {
            emptyList()
        }
    }

    suspend fun getUsers(): List<UserData> {
        return try {
            typicodeService.users()
        } catch (ignore: IOException) {
            emptyList()
        } catch (ignore: HttpException) {
            emptyList()
        }
    }

    suspend fun getComments(): List<CommentData> {
        return try {
            typicodeService.comments()
        } catch (ignore: IOException) {
            emptyList()
        } catch (ignore: HttpException) {
            emptyList()
        }
    }
}
