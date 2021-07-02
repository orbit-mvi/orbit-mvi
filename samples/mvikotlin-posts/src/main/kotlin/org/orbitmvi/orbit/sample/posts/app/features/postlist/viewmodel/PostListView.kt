package org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel

import com.arkivanov.mvikotlin.core.view.ViewRenderer
import org.orbitmvi.orbit.sample.posts.app.common.NavigationEvent

interface PostListView : ViewRenderer<PostListState> {
    fun sideEffect(navigationEvent: NavigationEvent)
}
