package com.babylon.orbit.sample

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.babylon.orbit.sample.domain.todo.Todo
import com.babylon.orbit.sample.domain.todo.TodoStatus
import com.babylon.orbit.sample.presentation.ScreenState
import com.babylon.orbit.sample.presentation.TodoScreenAction
import com.babylon.orbit.sample.presentation.TodoScreenReducer
import com.babylon.orbit.sample.presentation.TodoScreenState
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.io.IOException

class TodoScreenReducerSpek : Spek({
    val reducer by memoized { TodoScreenReducer() }

    given("a TodoStatus.Loading event") {
        val event = TodoStatus.Loading

        lateinit var todoScreenState: TodoScreenState
        on("applying the reducer") {
            todoScreenState = reducer.reduceLoadTodos(TodoScreenState(), event)

            it("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                    TodoScreenState(screenState = ScreenState.Loading)
                )
            }
        }
    }

    given("a TodoStatus.Failure event") {
        val event = TodoStatus.Failure(IOException())

        lateinit var todoScreenState: TodoScreenState
        on("applying the reducer") {
            todoScreenState = reducer.reduceLoadTodos(TodoScreenState(), event)

            it("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                    TodoScreenState(screenState = ScreenState.Error)
                )
            }
        }
    }

    given("a TodoStatus.Failure event") {
        val event = TodoStatus.Result(DUMMY_TODO_LIST)

        lateinit var todoScreenState: TodoScreenState
        on("applying the reducer") {
            todoScreenState = reducer.reduceLoadTodos(TodoScreenState(), event)

            it("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                    TodoScreenState(screenState = ScreenState.Ready, todoList = DUMMY_TODO_LIST)
                )
            }
        }
    }

    given("a TodoScreenAction.TodoSelected event") {
        val todoId = 2
        val event = TodoScreenAction.TodoSelected(todoId)

        lateinit var todoScreenState: TodoScreenState
        on("applying the reducer") {
            todoScreenState = reducer.reduceLoadSelectedTodo(TodoScreenState(), event)

            it("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                    TodoScreenState(todoSelectedId = todoId)
                )
            }
        }
    }

    given("a state with a selected todo event") {
        val todoId = 2

        lateinit var todoScreenState: TodoScreenState
        on("applying the reducer") {
            todoScreenState = reducer.reduceDismissSelectedTodo(
                TodoScreenState(todoSelectedId = todoId)
            )

            it("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(TodoScreenState())
            }
        }
    }
})

val DUMMY_TODO_LIST = listOf(
    Todo(1, 1, "first todo"),
    Todo(2, 2, "second todo"),
    Todo(3, 3, "third todo")
)
