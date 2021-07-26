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

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.sample.posts.R
import org.orbitmvi.orbit.sample.posts.app.common.SeparatorDecoration
import org.orbitmvi.orbit.sample.posts.app.common.viewBinding
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailState
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailsViewModel
import org.orbitmvi.orbit.sample.posts.databinding.PostDetailsFragmentBinding
import org.orbitmvi.orbit.viewmodel.observe

class PostDetailsFragment : Fragment(R.layout.post_details_fragment) {

    private val args: PostDetailsFragmentArgs by navArgs()
    private val viewModel: PostDetailsViewModel by stateViewModel { parametersOf(args.overview) }
    private var initialised: Boolean = false
    private val adapter = GroupAdapter<GroupieViewHolder>()

    private val binding by viewBinding<PostDetailsFragmentBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.postCommentsList.layoutManager = LinearLayoutManager(activity)
        ViewCompat.setNestedScrollingEnabled(binding.postCommentsList, false)

        binding.postCommentsList.addItemDecoration(
            SeparatorDecoration(
                requireActivity(),
                R.dimen.separator_margin_start,
                R.dimen.separator_margin_end
            )
        )

        binding.postCommentsList.adapter = adapter

        viewModel.observe(viewLifecycleOwner, state = ::render)
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
        }
        binding.postTitle.text = state.postOverview.title

        if (state is PostDetailState.Details) {
            binding.postBody.text = state.post.body

            val comments = state.post.comments.size
            binding.postCommentCount.text = context?.resources?.getQuantityString(
                R.plurals.comments,
                comments,
                comments
            )

            adapter.update(state.post.comments.map(::PostCommentItem))
        }
    }
}
