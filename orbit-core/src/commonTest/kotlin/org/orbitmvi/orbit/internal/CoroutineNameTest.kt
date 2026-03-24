package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CoroutineNameTest {

    @Test
    fun intent_coroutines_have_names_that_increment() = runTest {
        val middleware = Middleware(this)
        middleware.container.sideEffectFlow.test {
            assertEquals("orbit-intent-0", awaitItem())

            middleware.someIntent()

            assertEquals("orbit-intent-1", awaitItem())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(scope: TestScope) : OrbitContainerHost<TestState, TestState, String> {

        override val container = scope.backgroundScope.orbitContainer(TestState()) {
            postSideEffect(coroutineContext[CoroutineName]?.name ?: "unknown")
        }

        fun someIntent() = intent {
            postSideEffect(coroutineContext[CoroutineName]?.name ?: "unknown")
        }
    }
}
