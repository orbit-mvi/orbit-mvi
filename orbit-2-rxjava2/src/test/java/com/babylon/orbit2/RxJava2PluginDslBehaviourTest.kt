/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2

import com.appmattus.kotlinfixture.kotlinFixture
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class RxJava2PluginDslBehaviourTest {
    private val fixture = kotlinFixture()
    private val initialState = fixture<TestState>()

    @BeforeEach
    fun beforeEach() {
        Orbit.registerDslPlugins(RxJava2Plugin)
    }

    @AfterEach
    fun afterEach() {
        Orbit.resetPlugins()
    }

    @Test
    fun `single transformation flatmaps`() {
        val action = fixture<Int>()

        Middleware()
            .given(initialState)
            .whenever {
                single(action)
            }
            .then {
                states(
                    { TestState(action + 5) }
                )
            }
    }

    @Test
    fun `single transformation crashes if plugin is not included`() {
        val action = fixture<Int>()
        Orbit.resetPlugins()

        assertThrows<IllegalStateException> {
            Middleware()
                .given(initialState)
                .whenever {
                    single(action)
                }
                .then {
                    states(
                        { TestState(action + 5) }
                    )
                }
        }
    }

    @Test
    fun `non empty maybe transformation flatmaps`() {
        val action = fixture<Int>()

        Middleware()
            .given(initialState)
            .whenever {
                maybe(action)
            }
            .then {
                states(
                    { TestState(action + 5) }
                )
            }
    }

    @Test
    fun `non empty maybe transformation crashes if plugin is not included`() {
        val action = fixture<Int>()
        Orbit.resetPlugins()

        assertThrows<IllegalStateException> {
            Middleware()
                .given(initialState)
                .whenever {
                    maybe(action)
                }
                .then {
                    states(
                        { TestState(action + 5) }
                    )
                }
        }
    }

    @Test
    fun `empty maybe transformation flatmaps`() {
        val action = fixture<Int>()

        Middleware()
            .given(initialState)
            .whenever {
                maybeNot(action)
            }
            .then {}
    }

    @Test
    fun `empty maybe transformation crashes if plugin is not included`() {
        val action = fixture<Int>()
        Orbit.resetPlugins()

        assertThrows<IllegalStateException> {
            Middleware()
                .given(initialState)
                .whenever {
                    maybeNot(action)
                }
                .then {}
        }
    }

    @Test
    fun `completable transformation flatmaps`() {
        val action = fixture<Int>()

        Middleware()
            .given(initialState)
            .whenever {
                completable(action)
            }
            .then {
                states(
                    { TestState(action) }
                )
            }
    }

    @Test
    fun `completable transformation crashes if plugin is not included`() {
        val action = fixture<Int>()
        Orbit.resetPlugins()

        assertThrows<IllegalStateException> {
            Middleware()
                .given(initialState)
                .whenever {
                    completable(action)
                }
                .then {
                    states(
                        { TestState(action) }
                    )
                }
        }
    }

    @Test
    fun `observable transformation flatmaps`() {
        val action = fixture<Int>()

        Middleware()
            .given(initialState)
            .whenever {
                observable(action)
            }
            .then {
                states(
                    { TestState(action) },
                    { TestState(action + 1) },
                    { TestState(action + 2) },
                    { TestState(action + 3) }
                )
            }
    }

    @Test
    fun `observable transformation crashes if plugin is not included`() {
        val action = fixture<Int>()
        Orbit.resetPlugins()

        assertThrows<IllegalStateException> {
            Middleware()
                .given(initialState)
                .whenever {
                    observable(action)
                }
                .then {
                    states(
                        { TestState(action) },
                        { TestState(action + 1) },
                        { TestState(action + 2) },
                        { TestState(action + 3) }
                    )
                }
        }
    }

    private data class TestState(val id: Int)

    private class Middleware : Host<TestState, String> {
        override val container = Container.create<TestState, String>(TestState(42))

        fun single(action: Int) = orbit(action) {
            transformRx2Single {
                Single.just(event + 5)
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybe(action: Int) = orbit(action) {
            transformRx2Maybe {
                Maybe.just(event + 5)
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun maybeNot(action: Int) = orbit(action) {
            transformRx2Maybe {
                Maybe.empty<Int>()
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun completable(action: Int) = orbit(action) {
            transformRx2Completable {
                Completable.complete()
            }
                .reduce {
                    state.copy(id = event)
                }
        }

        fun observable(action: Int) = orbit(action) {
            transformRx2Observable {
                Observable.just(event, event + 1, event + 2, event + 3)
            }
                .reduce {
                    state.copy(id = event)
                }
        }
    }
}
