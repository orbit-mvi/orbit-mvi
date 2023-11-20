package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import kotlin.test.AfterTest
import kotlin.test.Test

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
internal class InfiniteFlowTest {

    private val scope = CoroutineScope(Job())

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun infinite_flow_can_be_tested() {
        val dispatcher = UnconfinedTestDispatcher()
        val middleware = InfiniteFlowMiddleware().liveTest {
            this.dispatcher = dispatcher
        }

        middleware.testIntent {
            incrementForever()
        }

        dispatcher.scheduler.advanceTimeBy(100000)

        middleware.assert(listOf(42)) {
            states(
                { listOf(42, 43) },
                { listOf(42, 43, 44) },
                { listOf(42, 43, 44, 45) }
            )
        }
    }

    private inner class InfiniteFlowMiddleware : ContainerHost<List<Int>, Nothing> {
        override val container: Container<List<Int>, Nothing> = scope.container(listOf(42))

        fun incrementForever() = intent {
            while (true) {
                delay(30000)
                reduce { state + (state.last() + 1) }
            }
        }
    }
}
