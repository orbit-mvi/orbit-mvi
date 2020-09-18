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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.babylon.orbit2.livedata.sideEffectLiveData
import com.babylon.orbit2.livedata.stateLiveData
import com.babylon.orbit2.sample.posts.R
import com.babylon.orbit2.sample.posts.app.common.SeparatorDecoration
import com.babylon.orbit2.sample.posts.app.features.postlist.viewmodel.OpenPostNavigationEvent
import com.babylon.orbit2.sample.posts.app.features.postlist.viewmodel.PostListViewModel
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.post_list_fragment.*
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class PostListFragment : Fragment() {

    private val viewModel: PostListViewModel by stateViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.post_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as AppCompatActivity?)?.supportActionBar?.apply {
            setTitle(R.string.app_name)
            setLogo(R.drawable.ic_orbit_toolbar)
        }

        content.layoutManager = LinearLayoutManager(activity)
        content.addItemDecoration(SeparatorDecoration(requireActivity(), R.dimen.separator_margin_start_icon, R.dimen.separator_margin_end))

        val adapter = GroupAdapter<GroupieViewHolder>()

        content.adapter = adapter

        viewModel.container.stateLiveData.observe(viewLifecycleOwner) {
            adapter.update(it.overviews.map { PostListItem(it, viewModel) })
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.container.sideEffectLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is OpenPostNavigationEvent ->
                    findNavController().navigate(PostListFragmentDirections.actionListFragmentToDetailFragment(it.post))
            }
        }
    }
}
