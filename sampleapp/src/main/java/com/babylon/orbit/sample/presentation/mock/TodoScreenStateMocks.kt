package com.babylon.orbit.sample.presentation.mock

import com.babylon.orbit.launcher.view.StateMock.Companion.stateMocks
import com.babylon.orbit.sample.domain.todo.Todo
import com.babylon.orbit.sample.presentation.ScreenState
import com.babylon.orbit.sample.presentation.TodoScreenState

object TodoScreenStateMocks {

    val mocks
        get() = stateMocks<TodoScreenState> {
            defaultState = TodoScreenState()

            state("Ready no Data") {
                copy(
                    screenState = ScreenState.Ready
                )
            }

            state("Ready with Data") {
                copy(
                    screenState = ScreenState.Ready,
                    todoList = listOf(
                        Todo(
                            userId = 12,
                            id = 1,
                            title = "Buy melon"
                        ),
                        Todo(
                            userId = 12,
                            id = 2,
                            title = "Buy milk"
                        ),
                        Todo(
                            userId = 12,
                            id = 3,
                            title = "Buy water"
                        )
                    )
                )
            }

            state("With selected id = 2") {
                copy(
                    screenState = ScreenState.Ready,
                    todoList = listOf(
                        Todo(
                            userId = 12,
                            id = 1,
                            title = "Buy melon"
                        ),
                        Todo(
                            userId = 12,
                            id = 2,
                            title = "Buy milk"
                        )
                    ),
                    todoSelectedId = 2
                )
            }

            state("Error") {
                copy(
                    screenState = ScreenState.Error
                )
            }
        }
}
