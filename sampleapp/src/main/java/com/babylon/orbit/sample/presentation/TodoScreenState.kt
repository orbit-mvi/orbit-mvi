package com.babylon.orbit.sample.presentation

import com.babylon.orbit.sample.domain.todo.Todo
import com.babylon.orbit.sample.domain.user.UserProfile

data class TodoScreenState(
    val screenState: ScreenState = ScreenState.Loading,
    val todoList: List<Todo>? = null,
    val todoSelectedId: Int? = null,
    val userProfile: UserProfile? = null
)

enum class ScreenState {
    Loading,
    Ready,
    Error
}
