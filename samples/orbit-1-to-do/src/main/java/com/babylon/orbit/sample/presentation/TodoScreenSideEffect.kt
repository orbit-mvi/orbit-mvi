package com.babylon.orbit.sample.presentation

import com.babylon.orbit.sample.domain.analytics.AnalyticsManager

class TodoScreenSideEffect(
    private val analyticsManager: AnalyticsManager
) {
    internal fun trackSelectedTodo(action: TodoScreenAction.TodoSelected) =
        analyticsManager.trackAnalytics(action.todoId.toString())
}
