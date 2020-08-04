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

package com.babylon.orbit2.sample.posts.app.features.postdetails.viewmodel

import android.os.Parcelable
import com.babylon.orbit2.sample.posts.domain.repositories.PostDetail
import com.babylon.orbit2.sample.posts.domain.repositories.PostOverview
import kotlinx.android.parcel.Parcelize

sealed class PostDetailState : Parcelable {

    abstract val postOverview: PostOverview

    @Parcelize
    data class Details(override val postOverview: PostOverview, val post: PostDetail) : PostDetailState()

    @Parcelize
    data class NoDetailsAvailable(override val postOverview: PostOverview) : PostDetailState()
}
