package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import kotlin.coroutines.coroutineContext
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class CoroutineNameTest {

    @Test
    fun `intent coroutines have names that increment`() = runTest {
        val middleware = Middleware(this)
        middleware.container.sideEffectFlow.test {
            assertEquals("orbit-intent-0", awaitItem())

            middleware.someIntent()

            assertEquals("orbit-intent-1", awaitItem())
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware(scope: TestScope) : ContainerHost<TestState, String> {

        override val container = scope.backgroundScope.container(TestState()) {
            postSideEffect(coroutineContext[CoroutineName]?.name ?: "unknown")
        }

        fun someIntent() = intent {
            postSideEffect(coroutineContext[CoroutineName]?.name ?: "unknown")
        }
    }
}
