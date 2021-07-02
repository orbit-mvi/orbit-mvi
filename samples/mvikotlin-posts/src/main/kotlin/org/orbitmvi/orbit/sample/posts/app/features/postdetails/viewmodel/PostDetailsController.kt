package org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel

import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.lifecycle.doOnDestroy
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository

@Suppress("EXPERIMENTAL_API_USAGE")
class PostDetailsController(
    private val lifecycle: Lifecycle,
    postRepository: PostRepository,
    val postOverview: PostOverview,
    stateKeeper: StateKeeper<PostDetailState>
) {
    private val store = PostDetailsStoreFactory(DefaultStoreFactory, postOverview, postRepository, stateKeeper).create()

    init {
        lifecycle.doOnDestroy(store::dispose)
    }

    fun onViewCreated(view: PostDetailsView) {
        bind(lifecycle, BinderLifecycleMode.START_STOP) {
            @Suppress("EXPERIMENTAL_API_USAGE")
            store.states bindTo view
        }
    }
}
