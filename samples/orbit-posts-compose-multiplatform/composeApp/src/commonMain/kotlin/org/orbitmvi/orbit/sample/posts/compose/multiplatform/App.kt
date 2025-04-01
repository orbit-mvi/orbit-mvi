package org.orbitmvi.orbit.sample.posts.compose.multiplatform

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.commonModule
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.features.postdetails.ui.PostDetailsScreen
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.app.features.postlist.ui.PostListScreen
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.repositories.PostOverview
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.detail.PostDetailsViewModel
import org.orbitmvi.orbit.sample.posts.compose.multiplatform.domain.viewmodel.list.PostListViewModel

// Define a List route that doesn't take any arguments
@Serializable
public object List

// Define a Detail route that takes a PostOverview
@Serializable
public class Detail private constructor(
    private val id: Int,
    private val avatarUrl: String,
    private val title: String,
    private val username: String
) {
    public fun toPostOverview(): PostOverview {
        return PostOverview(id, avatarUrl, title, username)
    }

    public companion object {
        public fun of(postOverview: PostOverview): Detail {
            return Detail(
                postOverview.id,
                postOverview.avatarUrl,
                postOverview.title,
                postOverview.username
            )
        }
    }
}

@Composable
@Preview
public fun App() {
    KoinApplication(application = { modules(commonModule()) }) {
        val navController = rememberNavController()
        NavHost(navController, startDestination = List) {
            composable<List> {
                val viewModel = koinViewModel<PostListViewModel>()
                PostListScreen(navController, viewModel)
            }
            composable<Detail> { backStackEntry ->
                val detail: Detail = backStackEntry.toRoute()

                val viewModel =
                    koinViewModel<PostDetailsViewModel>(parameters = { parametersOf(detail.toPostOverview()) })
                PostDetailsScreen(navController, viewModel)
            }
        }
    }
}
