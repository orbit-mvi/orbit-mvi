package com.babylon.orbit.launcher.screenlist.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.babylon.orbit.launcher.R
import com.babylon.orbit.launcher.screenlist.business.OrbitLauncherState
import com.babylon.orbit.launcher.screenlist.business.OrbitLauncherViewModel
import com.babylon.orbit.launcher.screenlist.business.ScreenState
import com.babylon.orbit.launcher.screenlist.business.Type
import com.babylon.orbit.launcher.screenlist.business.Type.ACTIVITY
import com.babylon.orbit.launcher.screenlist.business.Type.FRAGMENT
import com.babylon.orbit.launcher.screenlist.di.OrbitLauncherViewModelFactory
import com.babylon.orbit.launcher.screenlist.ui.item.HeaderItem
import com.babylon.orbit.launcher.screenlist.ui.item.OrbitViewItem
import com.babylon.orbit.launcher.screenlist.ui.item.SpaceItemDecoration
import com.babylon.orbit.launcher.screenlist.ui.item.show
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import kotlinx.android.synthetic.main.orbit_launcher_activity.*

class OrbitLauncherActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProviders
            .of(this, OrbitLauncherViewModelFactory(applicationContext))
            .get(OrbitLauncherViewModel::class.java)
    }

    private val activities = Section()
    private val fragments = Section()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.orbit_launcher_activity)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(getColor(this, android.R.color.white))

        val space = resources.getDimension(R.dimen.container_padding).toInt()
        val decoration = SpaceItemDecoration(horizontalSpacing = space, verticalSpacing = space)
        list.addItemDecoration(decoration)

        activities.setHeader(HeaderItem(ACTIVITY.toString()))
        fragments.setHeader(HeaderItem(FRAGMENT.toString()))

        list.apply {
            layoutManager = LinearLayoutManager(this@OrbitLauncherActivity)
            adapter = GroupAdapter<GroupieViewHolder>().apply {
                add(activities)
                add(fragments)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.connect(this, ::render)
    }

    private fun render(state: OrbitLauncherState) {
        progress.show(state.screenState == ScreenState.Loading)

        if (state.screenState == ScreenState.Ready) {
            state.views
                .map { OrbitViewItem(it, viewModel::sendAction) }
                .run {
                    update(ACTIVITY, activities)
                    update(FRAGMENT, fragments)
                }
        }
    }

    private fun List<OrbitViewItem>.update(type: Type, section: Section) =
        filter { it.orbitView.type == type }.let(section::update)
}
