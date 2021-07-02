package org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SuspendExecutor
import com.arkivanov.mvikotlin.keepers.statekeeper.StateKeeper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostDetail
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.domain.repositories.PostRepository
import org.orbitmvi.orbit.sample.posts.domain.repositories.Status

@Suppress("EXPERIMENTAL_API_USAGE")
internal class PostDetailsStoreFactory(
    private val storeFactory: StoreFactory,
    val postOverview: PostOverview,
    private val postRepository: PostRepository,
    val stateKeeper: StateKeeper<PostDetailState>
) {

    fun create() = object : Store<Nothing, PostDetailState, Nothing> by storeFactory.create(
        name = "PostDetailsStore",
        initialState = stateKeeper.consume() ?: PostDetailState.NoDetailsAvailable(postOverview),
        bootstrapper = SimpleBootstrapper(Unit),
        executorFactory = ::createExecutorFactory,
        reducer = ReducerImpl
    ) {}

    private fun createExecutorFactory() = ExecutorImpl(postOverview, postRepository)

    private object ReducerImpl : Reducer<PostDetailState, Status<PostDetail>> {
        override fun PostDetailState.reduce(result: Status<PostDetail>): PostDetailState =
            when (result) {
                is Status.Success -> PostDetailState.Details(postOverview, result.data)
                is Status.Failure -> PostDetailState.NoDetailsAvailable(postOverview)
            }
    }

    private class ExecutorImpl(val postOverview: PostOverview, private val postRepository: PostRepository) :
        SuspendExecutor<Nothing, Unit, PostDetailState, Status<PostDetail>, Nothing>() {

        override suspend fun executeAction(action: Unit, getState: () -> PostDetailState) = loadDetails()

        private suspend fun loadDetails() {
            val status = withContext(Dispatchers.Default) {
                postRepository.getDetail(postOverview.id)
            }
            dispatch(status)
        }
    }
}
