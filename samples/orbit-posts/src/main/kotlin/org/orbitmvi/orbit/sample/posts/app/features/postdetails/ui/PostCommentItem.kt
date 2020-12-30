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

package org.orbitmvi.orbit.sample.posts.app.features.postdetails.ui

import android.widget.TextView
import org.orbitmvi.orbit.sample.posts.R
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostComment
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

data class PostCommentItem(private val post: PostComment) : Item() {

    override fun isSameAs(other: com.xwray.groupie.Item<*>) = other is PostCommentItem && post.id == other.post.id

    override fun hasSameContentAs(other: com.xwray.groupie.Item<*>) = other is PostCommentItem && post == other.post

    override fun getLayout() = R.layout.post_comment_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.apply {
            findViewById<TextView>(R.id.comment_username).text = post.name
            findViewById<TextView>(R.id.comment_email).text = post.email
            findViewById<TextView>(R.id.comment_body).text = post.body
        }
    }
}
