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

package org.orbitmvi.orbit.sample.posts.app.di

import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.dsl.module
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailState
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailsController
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListController
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListState
import org.orbitmvi.orbit.sample.posts.data.posts.PostDataRepository
import org.orbitmvi.orbit.sample.posts.data.posts.network.AvatarUrlGenerator
import org.orbitmvi.orbit.sample.posts.data.posts.network.PostNetworkDataSource
import org.orbitmvi.orbit.sample.posts.data.posts.network.TypicodeService
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

fun module() = module {

    @Suppress("EXPERIMENTAL_API_USAGE")
    factory { (lifecycle: Lifecycle, stateKeeper: StateKeeper<PostListState>) ->
        PostListController(lifecycle, get(), stateKeeper)
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    factory { (lifecycle: Lifecycle, postOverview: PostOverview, stateKeeper: StateKeeper<PostDetailState>) ->
        PostDetailsController(lifecycle, get(), postOverview, stateKeeper)
    }

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
