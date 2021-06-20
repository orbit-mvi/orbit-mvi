/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.test
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
internal class ContainerExceptionHandlerTest {

    private val scope = CoroutineScope(Job() + CoroutineExceptionHandler { _, _ -> /*just be silent*/ })

    @AfterTest
    fun afterTest() {
        scope.cancel()
    }

    @Test
    fun `by default any exception breaks the scope`() = runBlocking {
        val initState = Random.nextInt()
        val container = scope.container<Int, Nothing>(
            initialState = initState
        )
        val testObserver = container.stateFlow.test()
        val newState = Random.nextInt()

        container.orbit {
            throw IllegalStateException()
        }
        container.orbit {
            reduce { newState }
        }

        testObserver.awaitCount(2, 1000L)
        assertEquals(listOf(initState), testObserver.values)
        assertEquals(false, scope.isActive)
    }

    @Test
    fun `with exception handler exceptions are caught`() {
        val initState = Random.nextInt()
        val exceptions = mutableListOf<Throwable>()
        val exceptionHandler = CoroutineExceptionHandler { _, throwable -> exceptions += throwable }
        val container = scope.container<Int, Nothing>(
            initialState = initState,
            settings = Container.Settings(
                exceptionHandler = exceptionHandler,
                orbitDispatcher = Dispatchers.Unconfined
            )
        )
        val newState = Random.nextInt()

        runBlocking {
            container.orbit {
                reduce { throw IllegalStateException() }
            }
            container.orbit {
                reduce { newState }
            }
        }

        assertEquals(newState, container.stateFlow.value)
        assertEquals(true, scope.isActive)
        assertEquals(1, exceptions.size)
        assertTrue { exceptions.first() is IllegalStateException }
    }
}
