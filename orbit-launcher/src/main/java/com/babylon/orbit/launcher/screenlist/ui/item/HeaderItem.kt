package com.babylon.orbit.launcher.screenlist.ui.item

import com.babylon.orbit.launcher.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.header_list_item.view.*

class HeaderItem(private val stateName: String) : Item() {

    override fun getLayout(): Int = R.layout.header_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        with(viewHolder.containerView) {
            header.text = stateName
        }
    }
}
