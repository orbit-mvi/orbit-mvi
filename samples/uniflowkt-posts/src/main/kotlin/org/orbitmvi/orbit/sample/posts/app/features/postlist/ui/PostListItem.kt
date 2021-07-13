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

package org.orbitmvi.orbit.sample.posts.app.features.postlist.ui

import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.xwray.groupie.viewbinding.BindableItem
import org.orbitmvi.orbit.sample.posts.R
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListViewModel
import org.orbitmvi.orbit.sample.posts.databinding.PostListItemBinding
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview

data class PostListItem(private val post: PostOverview, private val viewModel: PostListViewModel) : BindableItem<PostListItemBinding>() {

    override fun initializeViewBinding(view: View) = PostListItemBinding.bind(view)

    override fun isSameAs(other: com.xwray.groupie.Item<*>) = other is PostListItem && post.id == other.post.id

    override fun hasSameContentAs(other: com.xwray.groupie.Item<*>) = other is PostListItem && post == other.post

    override fun getLayout() = R.layout.post_list_item

    override fun bind(viewBinding: PostListItemBinding, position: Int) {
        Glide.with(viewBinding.root.context).load(post.avatarUrl)
            .apply(RequestOptions.circleCropTransform()).into(viewBinding.postAvatar)

        viewBinding.postTitle.text = post.title
        viewBinding.postUsername.text = post.username

        viewBinding.root.setOnClickListener {
            viewModel.onPostClicked(post)
        }
    }
}
