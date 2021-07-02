package org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel

import org.orbitmvi.orbit.sample.posts.domain.repositories.PostOverview

sealed class PostListIntent {
    data class PostClicked(val post: PostOverview) : PostListIntent()
}
