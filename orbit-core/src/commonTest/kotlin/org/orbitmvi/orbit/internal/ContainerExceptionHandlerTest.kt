/*
 * Copyright 2021-2023 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test.test
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ContainerExceptionHandlerTest {

    @Test
    fun by_default_exceptions_are_uncaught() {
        assertFailsWith<IllegalStateException> {
            runTest {
                ExceptionTestMiddleware(this).test(this) {
                    expectInitialState()

                    containerHost.exceptionIntent().join()
                }
            }
        }
    }

    @Test
    fun with_exception_handler_exceptions_are_caught() {
        val initState = Random.nextInt()
        val exceptions = Channel<Throwable>(capacity = Channel.BUFFERED)
        val exceptionHandler = CoroutineExceptionHandler { _, throwable -> exceptions.trySend(throwable) }

        runTest {
            val container = backgroundScope.container<Int, Nothing>(
                initialState = initState,
                buildSettings = {
                    this.exceptionHandler = exceptionHandler
                }
            )
            container.stateFlow.test {
                assertEquals(initState, awaitItem())

                container.orbit {
                    throw IllegalStateException()
                }
            }
            exceptions.consumeAsFlow().test {
                assertEquals(IllegalStateException::class, awaitItem()::class)
                cancel()
            }
        }
    }

    @Test
    fun with_exception_handler_test_does_not_break() = runTest {
        val initState = Random.nextInt()
        val exceptions = Channel<Throwable>(capacity = Channel.BUFFERED)
        val exceptionHandler = CoroutineExceptionHandler { _, throwable -> exceptions.trySend(throwable) }
        ExceptionTestMiddleware(this, exceptionHandler).test(this, initState) {
            expectInitialState()

            containerHost.exceptionIntent()

            exceptions.consumeAsFlow().test {
                assertEquals(IllegalStateException::class, awaitItem()::class)
                cancel()
            }
            cancelAndIgnoreRemainingItems()
        }
    }

    @Test
    fun with_exception_handler_cancellation_exception_is_propagated_normally() {
        checkCancellationPropagation(withExceptionHandler = true)
    }

    @Test
    fun without_exception_handler_cancellation_exception_is_propagated_normally() {
        checkCancellationPropagation(withExceptionHandler = false)
    }

    private fun checkCancellationPropagation(withExceptionHandler: Boolean) {
        val scopeJob = SupervisorJob()
        val containerScope = CoroutineScope(scopeJob)
        val exceptionHandler =
            if (withExceptionHandler) {
                // Don't care
                CoroutineExceptionHandler { _, _ -> }
            } else {
                null
            }
        val container = containerScope.container<Unit, Nothing>(
            initialState = Unit,
            buildSettings = {
                this.exceptionHandler = exceptionHandler
            }
        )
        val mutex = Mutex(locked = true)
        runTest {
            lateinit var job: Job
            container.orbit {
                coroutineScope {
                    job = launch {
                        mutex.unlock()
                        delay(Long.MAX_VALUE)
                    }
                }
            }
            mutex.withLock {
                scopeJob.cancelAndJoin()
                assertFalse { containerScope.isActive }
                println(job)
                assertTrue { job.isCancelled }
                assertFalse { job.isActive }
            }
        }
    }

    @Test
    fun without_exception_handler_test_does_break() {
        assertFailsWith<IllegalStateException> {
            runTest {
                ExceptionTestMiddleware(this).test(this) {
                    expectInitialState()

                    containerHost.exceptionIntent().join()
                }
            }
        }
    }

    private inner class ExceptionTestMiddleware(
        scope: TestScope,
        exceptionHandler: CoroutineExceptionHandler? = null
    ) : ContainerHost<Int, Nothing> {
        val initState = Random.nextInt()
        override val container = scope.backgroundScope.container<Int, Nothing>(
            initialState = initState,
            buildSettings = {
                this.exceptionHandler = exceptionHandler
            }
        )

        fun exceptionIntent() = intent {
            throw IllegalStateException()
        }
    }
}
