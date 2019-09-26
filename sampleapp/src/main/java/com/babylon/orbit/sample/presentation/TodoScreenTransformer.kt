package com.babylon.orbit.sample.presentation

import com.babylon.orbit.ActionState
import com.babylon.orbit.sample.domain.analytics.AnalyticsManager
import com.babylon.orbit.sample.domain.todo.GetTodoUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileSwitchesUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileUseCase
import com.babylon.orbit.sample.domain.user.UserProfileSwitchesStatus
import io.reactivex.Observable

class TodoScreenTransformer(
    private val todoUseCase: GetTodoUseCase,
    private val getUserProfileSwitchesUseCase: GetUserProfileSwitchesUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val analyticsManager: AnalyticsManager
) {

    internal fun loadTodos(actions: Observable<ActionState<TodoScreenState, Any>>) =
        actions.switchMap { todoUseCase.getTodoList() }

    internal fun trackSelectedTodo(
        actions: Observable<ActionState<TodoScreenState,
            TodoScreenAction.TodoSelected>>
    ) =
        actions.doOnNext { analyticsManager.trackAnalytics(it.action.todoId.toString()) }

    internal fun loadUserProfileSwitches(actions: Observable<ActionState<TodoScreenState, TodoScreenAction.TodoUserSelected>>) =
        actions.switchMap { actions ->
            getUserProfileSwitchesUseCase.getUserProfileSwitches()
                .map { UserProfileExtra(it, actions.action.userId) }
        }

    internal fun loadUserProfile(actions: Observable<ActionState<TodoScreenState, UserProfileExtra>>) =
        actions.filter { it.action.userProfileSwitchesStatus is UserProfileSwitchesStatus.Result }
            .switchMap { getUserProfileUseCase.getUserProfile(it.action.userId) }
}
