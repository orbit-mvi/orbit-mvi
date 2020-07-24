package com.babylon.orbit.sample.presentation

sealed class TodoScreenAction {

    object RetryAction : TodoScreenAction()

    data class TodoSelected(val todoId: Int) : TodoScreenAction()

    data class TodoUserSelected(val userId: Int) : TodoScreenAction()

    object TodoSelectedDismissed : TodoScreenAction()

    object UserSelectedDismissed : TodoScreenAction()
}
