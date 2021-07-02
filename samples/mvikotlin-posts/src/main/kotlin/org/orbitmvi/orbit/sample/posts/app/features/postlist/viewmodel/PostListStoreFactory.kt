package org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.sample.posts.app.common.NavigationEvent
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository

@Suppress("EXPERIMENTAL_API_USAGE")
internal class PostListStoreFactory(
    private val storeFactory: StoreFactory,
    private val postRepository: PostRepository,
    val stateKeeper: StateKeeper<PostListState>
) {

    fun create() = object : Store<PostListIntent, PostListState, NavigationEvent> by storeFactory.create(
        name = "PostListStore",
        initialState = stateKeeper.consume() ?: PostListState(),
        bootstrapper = SimpleBootstrapper(Unit),
        executorFactory = ::createExecutorFactory,
        reducer = ReducerImpl
    ) {}

    private fun createExecutorFactory() = ExecutorImpl(postRepository)

    private object ReducerImpl : Reducer<PostListState, List<PostOverview>> {
        override fun PostListState.reduce(result: List<PostOverview>): PostListState = copy(overviews = result)
    }

    private class ExecutorImpl(private val postRepository: PostRepository) :
        SuspendExecutor<PostListIntent, Unit, PostListState, List<PostOverview>, NavigationEvent>() {

        override suspend fun executeAction(action: Unit, getState: () -> PostListState) = loadOverviews()

        override suspend fun executeIntent(intent: PostListIntent, getState: () -> PostListState) = when (intent) {
            is PostListIntent.PostClicked -> postClicked(intent.post)
        }

        private fun postClicked(post: PostOverview) {
            publish(OpenPostNavigationEvent(post))
        }


        private suspend fun loadOverviews() {
            val overviews = withContext(Dispatchers.Default) {
                postRepository.getOverviews()
            }
            dispatch(overviews)
        }
    }
}
