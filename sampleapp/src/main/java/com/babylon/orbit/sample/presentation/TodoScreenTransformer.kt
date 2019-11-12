package com.babylon.orbit.sample.presentation

import com.babylon.orbit.sample.domain.todo.GetTodoUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileSwitchesUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileUseCase
import com.babylon.orbit.sample.domain.user.UserProfileSwitchesStatus
import io.reactivex.Observable

class TodoScreenTransformer(
    private val todoUseCase: GetTodoUseCase,
    private val getUserProfileSwitchesUseCase: GetUserProfileSwitchesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase
) {

    internal fun loadTodos(actions: Observable<Any>) =
        actions.switchMap { todoUseCase.getTodoList() }

    internal fun loadUserProfileSwitches(actions: Observable<TodoScreenAction.TodoUserSelected>) =
        actions.switchMap { event ->
            getUserProfileSwitchesUseCase.getUserProfileSwitches()
                .map { UserProfileExtra(it, event.userId) }
        }

    internal fun loadUserProfile(actions: Observable<UserProfileExtra>) =
        actions.filter { it.userProfileSwitchesStatus is UserProfileSwitchesStatus.Result }
            .switchMap { getUserProfileUseCase.getUserProfile(it.userId) }
}
