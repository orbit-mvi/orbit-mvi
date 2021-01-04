/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.rxjava3

import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.assert
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.strict.OrbitDslPlugins
import org.orbitmvi.orbit.syntax.strict.orbit
import org.orbitmvi.orbit.syntax.strict.reduce
import org.orbitmvi.orbit.syntax.strict.sideEffect
import org.orbitmvi.orbit.test
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.random.Random

@ExperimentalCoroutinesApi
internal class RxJava3DslPluginBehaviourTest {
    private val initialState = TestState()
    private val scope = TestCoroutineScope(Job())

    @BeforeEach
    fun beforeEach() {
        OrbitDslPlugins.reset() // Test for proper registration
    }

    @AfterEach
    fun afterEach() {
        scope.cleanupTestCoroutines()
        scope.cancel()
    }

    @Test
    fun `single transformation flatmaps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.single(action)

        middleware.assert(initialState) {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `non empty maybe transformation flatmaps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.maybe(action)

        middleware.assert(initialState) {
            states(
                { TestState(action + 5) }
            )
        }
    }

    @Test
    fun `empty maybe transformation flatmaps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.maybeNot(action)
    }

    @Test
    fun `completable transformation flatmaps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.completable(action)

        middleware.assert(initialState) {
            states(
                { TestState(action) }
            )
        }
    }

    @Test
    fun `observable transformation flatmaps`() {
        val action = Random.nextInt()
        val middleware = Middleware().test(initialState)

        middleware.observable(action)

        middleware.assert(initialState) {
            postedSideEffects(
                action.toString(),
                (action + 1).toString(),
                (action + 2).toString(),
                (action + 3).toString()
            )
        }
    }

    private data class TestState(val id: Int = Random.nextInt())

    private inner class Middleware : ContainerHost<TestState, String> {

        override val container = scope.container<TestState, String>(TestState(42))

        fun single(action: Int) = orbit {
            transformRx3Single {
                Single.just(action + 5)
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybe(action: Int) = orbit {
            transformRx3Maybe {
                Maybe.just(action + 5)
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybeNot(action: Int) = orbit {
            transformRx3Maybe {
                Maybe.empty<Int>()
            }
                .reduce {
                    state.copy(id = action)
                }
        }

        fun completable(action: Int) = orbit {
            transformRx3Completable {
                Completable.complete()
            }
                .reduce {
                    state.copy(id = action)
                }
        }

        fun observable(action: Int) = orbit {
            transformRx3Observable {
                Observable.just(action, action + 1, action + 2, action + 3)
            }
                .sideEffect {
                    post(event.toString())
                }
        }
    }
}
