package com.babylon.orbit.sample

import com.babylon.orbit.sample.domain.todo.Todo
import com.babylon.orbit.sample.domain.todo.TodoStatus
import com.babylon.orbit.sample.presentation.ScreenState
import com.babylon.orbit.sample.presentation.TodoScreenAction
import com.babylon.orbit.sample.presentation.TodoScreenReducer
import com.babylon.orbit.sample.presentation.TodoScreenState
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.io.IOException

class TodoScreenReducerSpek : Spek({
    val reducer by memoized { TodoScreenReducer() }

    Feature("todo screen reducers") {
        Scenario("reducing load todos loading event") {
            lateinit var todoScreenState: TodoScreenState
            lateinit var event: TodoStatus

            Given("a TodoStatus.Loading event") {
                event = TodoStatus.Loading
            }

            When("applying the reducer") {
                todoScreenState = reducer.reduceLoadTodos(TodoScreenState(), event)
            }

            Then("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                        TodoScreenState(screenState = ScreenState.Loading)
                )
            }
        }

        Scenario("reducing load todos failure event") {
            lateinit var todoScreenState: TodoScreenState
            lateinit var event: TodoStatus

            Given("a TodoStatus.Failure event") {
                event = TodoStatus.Failure(IOException())
            }

            When("applying the reducer") {
                todoScreenState = reducer.reduceLoadTodos(TodoScreenState(), event)
            }

            Then("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                        TodoScreenState(screenState = ScreenState.Error)
                )
            }
        }

        Scenario("a TodoStatus.Failure event") {
            lateinit var todoScreenState: TodoScreenState
            lateinit var event: TodoStatus

            Given("a TodoStatus.Failure event") {
                event = TodoStatus.Result(DUMMY_TODO_LIST)
            }

            When("applying the reducer") {
                todoScreenState = reducer.reduceLoadTodos(TodoScreenState(), event)
            }

            Then("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                        TodoScreenState(screenState = ScreenState.Ready, todoList = DUMMY_TODO_LIST)
                )
            }
        }

        Scenario("a TodoScreenAction.TodoSelected event") {
            lateinit var todoScreenState: TodoScreenState
            lateinit var event: TodoScreenAction.TodoSelected
            val todoId = 2

            Given("a TodoScreenAction.TodoSelected event") {
                event = TodoScreenAction.TodoSelected(todoId)
            }

            When("applying the reducer") {
                todoScreenState = reducer.reduceLoadSelectedTodo(TodoScreenState(), event)
            }

            Then("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(
                        TodoScreenState(todoSelectedId = todoId)
                )
            }
        }

        Scenario("a state with a selected todo event") {
            lateinit var todoScreenState: TodoScreenState
            val todoId = 2

            Given("a state with a selected todo event") {
                todoScreenState = TodoScreenState(todoSelectedId = todoId)
            }

            When("applying the reducer") {
                todoScreenState = reducer.reduceDismissSelectedTodo(
                        TodoScreenState(todoSelectedId = todoId)
                )
            }

            Then("should apply the correct state") {
                assertThat(todoScreenState).isEqualTo(TodoScreenState())
            }
        }
    }
})

private val DUMMY_TODO_LIST = listOf(
        Todo(1, 1, "first todo"),
        Todo(2, 2, "second todo"),
        Todo(3, 3, "third todo")
)
