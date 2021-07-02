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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arkivanov.mvikotlin.core.lifecycle.asMviLifecycle
import com.arkivanov.mvikotlin.keepers.statekeeper.get
import com.arkivanov.mvikotlin.keepers.statekeeper.getParcelableStateKeeperRegistry
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.sample.posts.R
import org.orbitmvi.orbit.sample.posts.app.common.NavigationEvent
import org.orbitmvi.orbit.sample.posts.app.common.SeparatorDecoration
import org.orbitmvi.orbit.sample.posts.app.common.viewBinding
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.OpenPostNavigationEvent
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListController
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListState
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListView
import org.orbitmvi.orbit.sample.posts.databinding.PostListFragmentBinding

class PostListFragment : Fragment(R.layout.post_list_fragment), PostListView {

    private val controller by inject<PostListController> {
        @Suppress("EXPERIMENTAL_API_USAGE")
        (parametersOf(lifecycle.asMviLifecycle(), getParcelableStateKeeperRegistry().get()))
    }

    private val binding by viewBinding<PostListFragmentBinding>()

    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.post_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        controller.onViewCreated(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setTitle(R.string.app_name)
            setLogo(R.drawable.ic_orbit_toolbar)
        }

        binding.content.layoutManager = LinearLayoutManager(activity)
        binding.content.addItemDecoration(
            SeparatorDecoration(requireActivity(), R.dimen.separator_margin_start_icon, R.dimen.separator_margin_end)
        )

        binding.content.adapter = adapter
    }

    override fun sideEffect(navigationEvent: NavigationEvent) {
        when (navigationEvent) {
            is OpenPostNavigationEvent ->
                findNavController().navigate(
                    PostListFragmentDirections.actionListFragmentToDetailFragment(
                        navigationEvent.post
                    )
                ).also {
                    println("navigating to detail")
                }
        }
    }

    override fun render(model: PostListState) {
        adapter.update(model.overviews.map { PostListItem(it, controller) })
    }
}
