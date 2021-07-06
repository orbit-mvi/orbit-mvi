package org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel

import com.ww.roxie.BaseAction

sealed class PostDetailsAction : BaseAction {
    object OnCreate : PostDetailsAction()
}
