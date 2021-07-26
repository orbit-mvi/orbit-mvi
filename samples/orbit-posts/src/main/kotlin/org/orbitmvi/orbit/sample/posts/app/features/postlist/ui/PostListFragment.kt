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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.stateViewModel
import org.orbitmvi.orbit.sample.posts.R
import org.orbitmvi.orbit.sample.posts.app.common.NavigationEvent
import org.orbitmvi.orbit.sample.posts.app.common.SeparatorDecoration
import org.orbitmvi.orbit.sample.posts.app.common.viewBinding
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.OpenPostNavigationEvent
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListState
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListViewModel
import org.orbitmvi.orbit.sample.posts.databinding.PostListFragmentBinding

class PostListFragment : Fragment(R.layout.post_list_fragment) {

    private val viewModel: PostListViewModel by stateViewModel()

    private val binding by viewBinding<PostListFragmentBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.post_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setTitle(R.string.app_name)
            setLogo(R.drawable.ic_orbit_toolbar)
        }

        binding.content.layoutManager = LinearLayoutManager(activity)
        binding.content.addItemDecoration(
            SeparatorDecoration(requireActivity(), R.dimen.separator_margin_start_icon, R.dimen.separator_margin_end)
        )

        val adapter = GroupAdapter<GroupieViewHolder>()

        binding.content.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.container.stateFlow.collect {
                reduce(adapter, it)
            }
        }
        lifecycleScope.launch {
            viewModel.container.sideEffectFlow.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED).collect {
                sideEffect(it)
            }
        }
    }

    private fun sideEffect(it: NavigationEvent) {
        when (it) {
            is OpenPostNavigationEvent ->
                findNavController().navigate(
                    PostListFragmentDirections.actionListFragmentToDetailFragment(
                        it.post
                    )
                )
        }
    }

    private fun reduce(
        adapter: GroupAdapter<GroupieViewHolder>,
        it: PostListState
    ) {
        adapter.update(it.overviews.map { PostListItem(it, viewModel) })
    }
}
