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

package org.orbitmvi.orbit.sample.posts.app.features.postdetails.ui

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import org.orbitmvi.orbit.sample.posts.R
import org.orbitmvi.orbit.sample.posts.databinding.PostCommentListItemBinding
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostComment

data class PostCommentItem(private val post: PostComment) : BindableItem<PostCommentListItemBinding>() {

    override fun initializeViewBinding(view: View) = PostCommentListItemBinding.bind(view)

    override fun isSameAs(other: com.xwray.groupie.Item<*>) = other is PostCommentItem && post.id == other.post.id

    override fun hasSameContentAs(other: com.xwray.groupie.Item<*>) = other is PostCommentItem && post == other.post

    override fun getLayout() = R.layout.post_comment_list_item

    override fun bind(viewBinding: PostCommentListItemBinding, position: Int) {
        viewBinding.commentUsername.text = post.name
        viewBinding.commentEmail.text = post.email
        viewBinding.commentBody.text = post.body
    }
}
