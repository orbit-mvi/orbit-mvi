/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail

import kotlinx.serialization.Serializable
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview

@Serializable
public sealed class PostDetailState {

    public abstract val postOverview: PostOverview

    @Serializable
    public data class Details(override val postOverview: PostOverview, val post: PostDetail) : PostDetailState()

    @Serializable
    public data class NoDetailsAvailable(override val postOverview: PostOverview) : PostDetailState()
}
