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

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.orbitmvi.orbit.sample.posts.data.posts.PostDataRepository
import org.orbitmvi.orbit.sample.posts.data.posts.network.TypicodeService
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    fun provideObjectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule().configure(FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Provides
    fun provideRetrofit(objectMapper: ObjectMapper): Retrofit = Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .baseUrl("https://jsonplaceholder.typicode.com").build()

    @Provides
    fun provideTypicodeService(retrofit: Retrofit): TypicodeService = retrofit.create(TypicodeService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindModule {

    @Binds
    abstract fun bindPostDataRepository(
        postDataRepository: PostDataRepository
    ): PostRepository
}

/*import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailsViewModel
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListViewModel
import org.orbitmvi.orbit.sample.posts.data.posts.PostDataRepository
import org.orbitmvi.orbit.sample.posts.data.posts.network.AvatarUrlGenerator
import org.orbitmvi.orbit.sample.posts.data.posts.network.PostNetworkDataSource
import org.orbitmvi.orbit.sample.posts.data.posts.network.TypicodeService
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

fun module() = module {
    viewModel { PostListViewModel(get(), get()) }

    viewModel { (postOverview: PostOverview) -> PostDetailsViewModel(get(), get(), postOverview) }


    single<PostRepository> { PostDataRepository(get(), get()) }
}
*/
