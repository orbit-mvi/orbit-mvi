package com.babylon.orbit.launcher.screenlist.ui.item

import android.widget.TextView
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class StateItem(
    private val stateName: String,
    private val clickListener: (String) -> Unit
) : Item() {

    override fun getLayout(): Int = android.R.layout.simple_selectable_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        with(viewHolder.containerView) {
            setOnClickListener {
                clickListener.invoke(stateName)
            }

            findViewById<TextView>(android.R.id.text1).text = stateName
        }
    }
}
