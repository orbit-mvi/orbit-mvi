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

package com.babylon.orbit2.sample.posts.app.features.postdetails.ui

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.babylon.orbit2.livedata.stateLiveData
import com.babylon.orbit2.sample.posts.R
import com.babylon.orbit2.sample.posts.app.common.SeparatorDecoration
import com.babylon.orbit2.sample.posts.app.features.postdetails.viewmodel.PostDetailState
import com.babylon.orbit2.sample.posts.app.features.postdetails.viewmodel.PostDetailsViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.post_details_fragment.*
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.core.parameter.parametersOf

class PostDetailsFragment : Fragment() {

    private val args: PostDetailsFragmentArgs by navArgs()
    private val viewModel: PostDetailsViewModel by stateViewModel { parametersOf(args.overview) }
    private var initialised: Boolean = false
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.post_details_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        post_comments_list.layoutManager = LinearLayoutManager(activity)
        ViewCompat.setNestedScrollingEnabled(post_comments_list, false)

        post_comments_list.addItemDecoration(
            SeparatorDecoration(
                requireActivity(),
                R.dimen.separator_margin_start,
                R.dimen.separator_margin_end
            )
        )

        post_comments_list.adapter = adapter

        viewModel.container.stateLiveData.observe(viewLifecycleOwner) { render(it) }
    }

    private fun render(state: PostDetailState) {
        if (!initialised) {
            initialised = true
            (activity as AppCompatActivity?)?.supportActionBar?.apply {
                title = state.postOverview.username
                Glide.with(requireContext()).load(state.postOverview.avatarUrl)
                    .apply(RequestOptions.overrideOf(resources.getDimensionPixelSize(R.dimen.toolbar_logo_size)))
                    .apply(RequestOptions.circleCropTransform()).into(
                        object : CustomTarget<Drawable>() {
                            override fun onLoadCleared(placeholder: Drawable?) {
                                placeholder?.let(::setLogo)
                            }

                            override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                val logo = LayerDrawable(arrayOf(resource)).apply {
                                    setLayerInset(0, 0, 0, resources.getDimensionPixelSize(R.dimen.toolbar_logo_padding_end), 0)
                                }

                                setLogo(logo)
                            }
                        }
                    )
            }
            post_title.text = state.postOverview.title
        }

        if (state is PostDetailState.Details) {
            post_body.text = state.post.body

            val comments = state.post.comments.size
            post_comment_count.text = context?.resources?.getQuantityString(
                R.plurals.comments,
                comments,
                comments
            )

            adapter.update(state.post.comments.map(::PostCommentItem))
        }
    }
}
