package com.babylon.orbit.sample.presentation

import android.os.Parcelable
import com.babylon.orbit.sample.domain.todo.Todo
import com.babylon.orbit.sample.domain.user.UserProfile
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TodoScreenState(
    val screenState: ScreenState = ScreenState.Loading,
    val todoList: List<Todo>? = null,
    val todoSelectedId: Int? = null,
    val userProfile: UserProfile? = null
) : Parcelable

enum class ScreenState {
    Loading,
    Ready,
    Error
}
