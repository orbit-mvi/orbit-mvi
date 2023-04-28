/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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

//import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
//import org.orbitmvi.orbit.ContainerHost
//import org.orbitmvi.orbit.annotation.OrbitExperimental
//import org.orbitmvi.orbit.container
//import org.orbitmvi.orbit.syntax.simple.intent
//import org.orbitmvi.orbit.syntax.simple.postSideEffect
//import org.orbitmvi.orbit.syntax.simple.reduce
//import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.intent

//import kotlin.test.assertEquals
//import kotlin.test.assertFailsWith
//import kotlin.test.assertTrue
//import kotlinx.coroutines.CoroutineExceptionHandler
//import kotlinx.coroutines.delay
//
//@OptIn(OrbitExperimental::class)
@ExperimentalCoroutinesApi
class ExceptionTest {

    private val initialState = State()

    @OptIn(OrbitExperimental::class)
    @Test
    fun `succeeds if initial state matches expected state`() = runTest {

//        assertFails {
            ExceptionTestMiddleware(this).test(this) {
                expectInitialState()

                val job = invokeIntent { boom() }

                job.join()
            }
//        }

//
//        ExceptionTestMiddleware(this).boom()
    }

    private inner class ExceptionTestMiddleware(scope: CoroutineScope) : ContainerHost<State, Int> {
        override val container = scope.container<State, Int>(initialState)

        fun boom() = intent {
            throw IllegalStateException("Boom!")
        }
    }

    private data class State(
        val count: Int = Random.nextInt(),
        val list: List<Int> = emptyList()
    )
}
