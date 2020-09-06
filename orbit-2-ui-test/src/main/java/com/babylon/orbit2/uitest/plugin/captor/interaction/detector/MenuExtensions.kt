package com.babylon.orbit2.uitest.plugin.captor.interaction.detector

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.children
import com.babylon.orbit2.uitest.plugin.captor.interaction.PendingInteraction

internal val Menu.pendingInteractions: Sequence<PendingInteraction>
    get() = children.mapNotNull { menuItem ->
        menuItem.menuItemClickListener?.let { clickListener ->
            menuItem.pendingInteraction { clickListener.onMenuItemClick(menuItem) }
        }
    }

internal fun MenuItem.pendingInteraction(executor: () -> Unit) = PendingInteraction(
    source = title.toString(),
    event = "menu item click",
    executor = executor
)

private val MenuItem.menuItemClickListener: MenuItem.OnMenuItemClickListener?
    get() = MenuItemImpl::class.java.getDeclaredField("mClickListener").apply {
        isAccessible = true
    }.get(this) as? MenuItem.OnMenuItemClickListener
