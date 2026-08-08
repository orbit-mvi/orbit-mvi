/*
 * Copyright 2023-2026 Mikołaj Leszczyński & Appmattus Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.orbitmvi.orbit.test

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

class ExceptionTest {

    private val initialState = InternalState()

    @Test
    fun exceptions_thrown_during_test_can_be_asserted_on() = runTest {
        assertFails {
            ExceptionTestMiddleware(this).testWithInternalState(this) {
                val job = containerHost.boom()

                job.join()
            }
        }
    }

    @Test
    fun exceptions_thrown_while_awaiting_internal_state_are_surfaced() = runTest {
        val exception = assertFailsWith<IllegalStateException> {
            ExceptionTestMiddleware(this).testWithInternalState(this) {
                containerHost.boom()

                awaitInternalState()
            }
        }

        assertEquals("Boom!", exception.message)
    }

    @Test
    fun exceptions_thrown_while_awaiting_external_state_are_surfaced() = runTest {
        val exception = assertFailsWith<IllegalStateException> {
            ExceptionTestMiddleware(this).testWithExternalState(this) {
                containerHost.boom()

                awaitExternalState()
            }
        }

        assertEquals("Boom!", exception.message)
    }

    @Test
    fun exceptions_thrown_while_awaiting_internal_and_external_state_are_surfaced() = runTest {
        val exception = assertFailsWith<IllegalStateException> {
            ExceptionTestMiddleware(this).testWithInternalAndExternalState(this) {
                containerHost.boom()

                awaitInternalState()
            }
        }

        assertEquals("Boom!", exception.message)
    }

    private inner class ExceptionTestMiddleware(scope: TestScope) :
        OrbitContainerHost<InternalState, ExternalState, Int> {
        override val container: OrbitContainer<InternalState, ExternalState, Int> = scope.backgroundScope.orbitContainer(
            initialState,
            ::transformState
        )

        private fun transformState(internalState: InternalState): ExternalState = ExternalState(internalState.count.toString())

        fun boom() = intent {
            throw IllegalStateException("Boom!")
        }
    }

    private data class InternalState(
        val count: Int = Random.nextInt(),
        val list: List<Int> = emptyList()
    )

    private data class ExternalState(val count: String)
}
