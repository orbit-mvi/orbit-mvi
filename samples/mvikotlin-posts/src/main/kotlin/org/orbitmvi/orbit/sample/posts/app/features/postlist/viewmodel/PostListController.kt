package org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel

import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository

@Suppress("EXPERIMENTAL_API_USAGE")
class PostListController(
    private val lifecycle: Lifecycle,
    postRepository: PostRepository,
    stateKeeper: StateKeeper<PostListState>
) {
    private val store = PostListStoreFactory(DefaultStoreFactory, postRepository, stateKeeper).create()
    private var binder: Binder? = null

    init {
        lifecycle.doOnDestroy(store::dispose)
    }

    fun onPostClicked(post: PostOverview) {
        store.accept(PostListIntent.PostClicked(post))
    }

    fun onViewCreated(view: PostListView) {
        binder?.stop()
        binder = bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            @Suppress("EXPERIMENTAL_API_USAGE")
            store.states bindTo view
            store.labels bindTo view::sideEffect
        }
    }
}
