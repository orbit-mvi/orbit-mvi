package com.babylon.orbit.sample.presentation

import com.babylon.orbit.sample.domain.todo.TodoStatus
import com.babylon.orbit.sample.domain.user.UserProfileStatus
import com.babylon.orbit.sample.domain.user.UserProfileSwitchesStatus

class TodoScreenReducer {

    internal fun reduceLoadTodos(
        currentState: TodoScreenState,
        event: TodoStatus
    ): TodoScreenState {
        return when (event) {
            is TodoStatus.Loading -> currentState.copy(screenState = ScreenState.Loading)
            is TodoStatus.Failure -> currentState.copy(screenState = ScreenState.Error)
            is TodoStatus.Result ->
                currentState.copy(screenState = ScreenState.Ready, todoList = event.todoList)
        }
    }

    internal fun reduceLoadSelectedTodo(
        currentState: TodoScreenState,
        event: TodoScreenAction.TodoSelected
    ): TodoScreenState {
        return currentState.copy(todoSelectedId = event.todoId)
    }

    internal fun reduceDismissSelectedTodo(currentState: TodoScreenState): TodoScreenState {
        return currentState.copy(todoSelectedId = null)
    }

    internal fun reduceLoadUserProfile(
        currentState: TodoScreenState,
        event: UserProfileStatus
    ): TodoScreenState {
        return when (event) {
            is UserProfileStatus.Loading -> currentState.copy(screenState = ScreenState.Loading)
            is UserProfileStatus.Failure -> currentState.copy(screenState = ScreenState.Error)
            is UserProfileStatus.Result ->
                currentState.copy(screenState = ScreenState.Ready, userProfile = event.userProfile)
        }
    }

    internal fun reduceLoadUserProfileSwitch(
        currentState: TodoScreenState,
        event: UserProfileExtra
    ): TodoScreenState {
        return when (event.userProfileSwitchesStatus) {
            is UserProfileSwitchesStatus.Loading -> currentState.copy(screenState = ScreenState.Loading)
            is UserProfileSwitchesStatus.Failure -> currentState.copy(screenState = ScreenState.Error)
            is UserProfileSwitchesStatus.Result -> currentState
        }
    }

    internal fun reduceUserSelected(currentState: TodoScreenState): TodoScreenState {
        return currentState.copy(userProfile = null)
    }
}
