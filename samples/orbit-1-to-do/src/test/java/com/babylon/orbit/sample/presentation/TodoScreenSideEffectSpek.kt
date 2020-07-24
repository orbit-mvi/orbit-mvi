package com.babylon.orbit.sample.presentation

import com.babylon.orbit.sample.domain.analytics.AnalyticsManager
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature

internal class TodoScreenSideEffectSpek : Spek({
    Feature("Todo side effects") {
        val mockAnalyticsManager by memoized { mock<AnalyticsManager>() }
        val todoSideEffect by memoized { TodoScreenSideEffect(mockAnalyticsManager) }

        Scenario("track todo selected") {
            val todoId = 333
            lateinit var event: TodoScreenAction.TodoSelected

            Given("a todo selected action") {
                event = TodoScreenAction.TodoSelected(todoId)
            }

            When("passing the event into the side effect handler") {
                todoSideEffect.trackSelectedTodo(event)
            }

            Then("should trigger the correct action") {
                verify(mockAnalyticsManager).trackAnalytics(todoId.toString())
            }
        }
    }
})
