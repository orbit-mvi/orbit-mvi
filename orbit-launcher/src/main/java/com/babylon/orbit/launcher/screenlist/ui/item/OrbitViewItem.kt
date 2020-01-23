package com.babylon.orbit.launcher.screenlist.ui.item

import com.babylon.orbit.launcher.R
import com.babylon.orbit.launcher.screenlist.business.OrbitViewAction
import com.babylon.orbit.launcher.screenlist.business.PresentationOrbitView
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.orbit_launcher_view_item.view.*

internal data class OrbitViewItem(
    val orbitView: PresentationOrbitView,
    private val clickListener: (Any) -> Unit
) : Item() {

    override fun getId() = orbitView.hashCode().toLong()

    override fun getLayout() = R.layout.orbit_launcher_view_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        with(viewHolder.containerView) {
            setOnClickListener {
                clickListener(OrbitViewAction.Selected(orbitView))
            }

            name.text = orbitView.name
            owner.text = orbitView.owner
        }
    }
}
