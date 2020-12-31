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

package org.orbitmvi.orbit.sample.posts.app.di

import androidx.lifecycle.SavedStateHandle
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailsViewModel
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListViewModel
import org.orbitmvi.orbit.sample.posts.data.posts.PostDataRepository
import org.orbitmvi.orbit.sample.posts.data.posts.network.AvatarUrlGenerator
import org.orbitmvi.orbit.sample.posts.data.posts.network.PostNetworkDataSource
import org.orbitmvi.orbit.sample.posts.data.posts.network.TypicodeService
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

fun module() = module {
    viewModel { (savedStateHandle: SavedStateHandle) -> PostListViewModel(savedStateHandle, get()) }

    viewModel { (savedStateHandle: SavedStateHandle, postOverview: PostOverview) -> PostDetailsViewModel(savedStateHandle, get(), postOverview) }

    single {
        ObjectMapper().registerKotlinModule().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    single {
        Retrofit.Builder()
            .addConverterFactory(JacksonConverterFactory.create(get()))
            .baseUrl("https://jsonplaceholder.typicode.com").build()
    }

    single { get<Retrofit>().create(TypicodeService::class.java) }

    single { PostNetworkDataSource(get()) }

    single { AvatarUrlGenerator() }

    single<PostRepository> { PostDataRepository(get(), get()) }
}
