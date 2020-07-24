package com.babylon.orbit.sample.presentation.ui

import com.babylon.orbit.sample.R
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item

class LogoItem : Item() {

    override fun getLayout() = R.layout.logo_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) = Unit
}
