package com.babylon.orbit.sample

import com.babylon.orbit.ActionState
import com.babylon.orbit.LifecycleAction
import com.babylon.orbit.sample.domain.analytics.AnalyticsManager
import com.babylon.orbit.sample.domain.todo.GetTodoUseCase
import com.babylon.orbit.sample.presentation.TodoScreenAction
import com.babylon.orbit.sample.presentation.TodoScreenState
import com.babylon.orbit.sample.presentation.TodoScreenTransformer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Observable
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

class TodoScreenTransformerSpek : Spek({

    val mockGetTodoUseCase by memoized { mock<GetTodoUseCase>() }
    val mockAnalyticsManager by memoized { mock<AnalyticsManager>() }
    val transformer by memoized {
        TodoScreenTransformer(mockGetTodoUseCase, mock(), mock(), mockAnalyticsManager)
    }

    given("a list of events") {
        val events = listOf(
            LifecycleAction.Created,
            TodoScreenAction.RetryAction
        )

        events.forEach { event ->
            on("passing the event named ${event.javaClass} into the transformer") {
                transformer.loadTodos(createActionState(TodoScreenState(), event)).test()

                it("should trigger the correct action") {
                    verify(mockGetTodoUseCase).getTodoList()
                }
            }
        }
    }

    given("a TodoScreenAction.TodoSelected event") {
        val todoId = 333
        val event = TodoScreenAction.TodoSelected(todoId)

        on("passing the event into the transformer") {
            transformer.trackSelectedTodo(createActionState(TodoScreenState(), event)).test()

            it("should trigger the correct action") {
                verify(mockAnalyticsManager).trackAnalytics(todoId.toString())
            }
        }
    }
})

private fun <ACTION : Any, STATE : Any> createActionState(action: ACTION, state: STATE) = Observable.just(
    ActionState(action, state)
)
