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

package com.babylon.orbit2.sample.posts.app.features.postlist.ui

import android.widget.ImageView
import android.widget.TextView
import com.babylon.orbit2.sample.posts.R
import com.babylon.orbit2.sample.posts.app.features.postlist.viewmodel.PostListViewModel
import com.babylon.orbit2.sample.posts.domain.repositories.PostOverview
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

data class PostListItem(private val post: PostOverview, private val viewModel: PostListViewModel) : Item() {

    override fun isSameAs(other: com.xwray.groupie.Item<*>) = other is PostListItem && post.id == other.post.id

    override fun hasSameContentAs(other: com.xwray.groupie.Item<*>) = other is PostListItem && post == other.post

    override fun getLayout() = R.layout.post_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val avatar: ImageView = viewHolder.itemView.findViewById(R.id.post_avatar)
        val title: TextView = viewHolder.itemView.findViewById(R.id.post_title)
        val username: TextView = viewHolder.itemView.findViewById(R.id.post_username)

        Glide.with(viewHolder.itemView.context).load(post.avatarUrl)
            .apply(RequestOptions.circleCropTransform()).into(avatar)

        title.text = post.title
        username.text = post.username

        viewHolder.itemView.setOnClickListener {
            viewModel.onPostClicked(post)
        }
    }
}
