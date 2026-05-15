/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.annotation.OrbitExperimental
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(OrbitExperimental::class)
class CombineContainerHostTest {

    private class IntHost(scope: CoroutineScope, initial: Int = 0) : OrbitContainerHost<Int, Int, Int> {
        override val container: OrbitContainer<Int, Int, Int> = scope.orbitContainer(initial)

        fun set(value: Int) = intent { reduce { value } }

        @OptIn(OrbitExperimental::class)
        fun postEffect(value: Int) = intent { postSideEffect(value) }
    }

    private class StringHost(scope: CoroutineScope, initial: String = "") : OrbitContainerHost<String, String, String> {
        override val container: OrbitContainer<String, String, String> = scope.orbitContainer(initial)

        fun set(value: String) = intent { reduce { value } }

        @OptIn(OrbitExperimental::class)
        fun postEffect(value: String) = intent { postSideEffect(value) }
    }

    @Test
    fun `2-arity state combines initial values`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)

        val combined = a.combine(b) { x, y -> x + y }

        combined.container.externalStateFlow.test {
            assertEquals(3, awaitItem())
        }
    }

    @Test
    fun `2-arity state updates propagate`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)

        val combined = a.combine(b) { x, y -> "$x-$y" }

        combined.container.externalStateFlow.test {
            assertEquals("1-2", awaitItem())
            a.set(10)
            assertEquals("10-2", awaitItem())
            b.set(20)
            assertEquals("10-20", awaitItem())
        }
    }

    @Test
    fun `2-arity combined state value is computed synchronously`() {
        runTest {
            val a = IntHost(backgroundScope, 3)
            val b = IntHost(backgroundScope, 4)

            val combined = a.combine(b) { x, y -> x * y }

            assertEquals(12, combined.container.externalStateFlow.value)
        }
    }

    @Test
    fun `2-arity combined state is distinct`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)

        // The transform maps to a constant — every upstream change collapses to "x".
        val combined = a.combine(b) { _, _ -> "x" }

        combined.container.externalStateFlow.test {
            assertEquals("x", awaitItem())
            a.set(1).join()
            b.set(2).join()
            expectNoEvents()
        }
    }

    @Test
    fun `2-arity no-side-effects overload emits nothing on side-effect flow`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)

        val combined = a.combine(b) { x, y -> x + y }

        combined.container.sideEffectFlow.test {
            a.postEffect(7)
            b.postEffect(9)
            awaitComplete()
        }
    }

    @Test
    fun `2-arity side-effect transform merges upstream effects`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = StringHost(backgroundScope, "")

        val combined = a.combine(
            other = b,
            transformState = { x, y -> "$x|$y" },
            transformSideEffects = { se1, se2 ->
                emitAll(merge(se1.map { "a:$it" }, se2.map { "b:$it" }))
            }
        )

        combined.container.sideEffectFlow.test {
            a.postEffect(1)
            assertEquals("a:1", awaitItem())
            b.postEffect("hi")
            assertEquals("b:hi", awaitItem())
        }
    }

    @Test
    fun `2-arity side-effect transform can filter`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)

        val combined = a.combine(
            other = b,
            transformState = { x, y -> x + y },
            transformSideEffects = { se1, _ ->
                emitAll(se1.filter { it % 2 == 0 })
            }
        )

        combined.container.sideEffectFlow.test {
            a.postEffect(1) // dropped
            a.postEffect(2)
            assertEquals(2, awaitItem())
            a.postEffect(3) // dropped
            a.postEffect(4)
            assertEquals(4, awaitItem())
        }
    }

    @Test
    fun `2-arity side-effect transform refCount flow mirrors emissions`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)

        val combined = a.combine(
            other = b,
            transformState = { x, y -> x + y },
            transformSideEffects = { se1, se2 -> emitAll(merge(se1, se2)) }
        )

        combined.container.refCountSideEffectFlow.test {
            a.postEffect(5)
            assertEquals(5, awaitItem())
            b.postEffect(6)
            assertEquals(6, awaitItem())
        }
    }

    @Test
    fun `3-arity receiver state combine`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)
        val c = IntHost(backgroundScope, 3)

        val combined = a.combine(b, c) { x, y, z -> x + y + z }

        combined.container.externalStateFlow.test {
            assertEquals(6, awaitItem())
            c.set(30)
            assertEquals(33, awaitItem())
        }
    }

    @Test
    fun `4-arity receiver state combine`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)
        val c = IntHost(backgroundScope, 3)
        val d = IntHost(backgroundScope, 4)

        val combined = a.combine(b, c, d) { x, y, z, w -> x + y + z + w }

        combined.container.externalStateFlow.test {
            assertEquals(10, awaitItem())
        }
    }

    @Test
    fun `5-arity receiver state combine`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)
        val c = IntHost(backgroundScope, 3)
        val d = IntHost(backgroundScope, 4)
        val e = IntHost(backgroundScope, 5)

        val combined = a.combine(b, c, d, e) { v, w, x, y, z -> v + w + x + y + z }

        combined.container.externalStateFlow.test {
            assertEquals(15, awaitItem())
        }
    }

    @Test
    fun `top-level state combine uses provided scope`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)
        val c = IntHost(backgroundScope, 3)

        val combined = combine(backgroundScope, a, b, c) { x, y, z -> x + y + z }

        combined.container.externalStateFlow.test {
            assertEquals(6, awaitItem())
        }
    }

    @Test
    fun `3-arity receiver side-effect transform merges all three`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)
        val c = StringHost(backgroundScope, "")

        val combined = a.combine(
            host2 = b,
            host3 = c,
            transformState = { x, y, z -> "$x-$y-$z" },
            transformSideEffects = { se1, se2, se3 ->
                emitAll(
                    merge(
                        se1.map { "a:$it" },
                        se2.map { "b:$it" },
                        se3.map { "c:$it" }
                    )
                )
            }
        )

        combined.container.sideEffectFlow.test {
            a.postEffect(1)
            assertEquals("a:1", awaitItem())
            c.postEffect("z")
            assertEquals("c:z", awaitItem())
            b.postEffect(2)
            assertEquals("b:2", awaitItem())
        }
    }

    @Test
    fun `chained combine propagates state and side effects`() = runTest {
        val a = IntHost(backgroundScope, 1)
        val b = IntHost(backgroundScope, 2)
        val c = IntHost(backgroundScope, 3)

        val first = a.combine(
            other = b,
            transformState = { x, y -> x + y },
            transformSideEffects = { se1, se2 -> emitAll(merge(se1, se2)) }
        )

        val second = first.combine(
            other = c,
            transformState = { sum, z -> sum * z },
            transformSideEffects = { se1, se2 -> emitAll(merge(se1, se2)) }
        )

        second.container.externalStateFlow.test {
            assertEquals(9, awaitItem()) // (1+2)*3
            a.set(4)
            assertEquals(18, awaitItem()) // (4+2)*3
        }

        second.container.sideEffectFlow.test {
            a.postEffect(100)
            assertEquals(100, awaitItem())
            c.postEffect(200)
            assertEquals(200, awaitItem())
        }
    }

    @Test
    fun `combined orbit throws UnsupportedOperationException`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)

        val combined = a.combine(b) { x, y -> x + y }

        assertFails {
            combined.container.orbit { }
        }
    }

    @Test
    fun `combined side-effect subscribers gate upstream repeatOnSubscription`() = runTest {
        // Host that posts an effect from inside repeatOnSubscription. The block only runs while
        // someone is subscribed to the host's refCountSideEffectFlow.
        val host = object : OrbitContainerHost<Int, Int, Int> {
            override val container: OrbitContainer<Int, Int, Int> = backgroundScope.orbitContainer(0)

            fun postOnSubscribe(value: Int) = intent {
                repeatOnSubscription {
                    postSideEffect(value)
                }
            }
        }
        val other = IntHost(backgroundScope, 0)

        val combined = host.combine(
            other = other,
            transformState = { x, y -> x + y },
            transformSideEffects = { se1, se2 -> emitAll(merge(se1, se2)) }
        )

        // No subscriber to combined yet → the upstream repeatOnSubscription should not fire.
        host.postOnSubscribe(42)

        // Subscribing to combined.sideEffectFlow gates upstream subscription on, so the effect arrives.
        combined.container.sideEffectFlow.test {
            assertEquals(42, awaitItem())
        }

        // After the test {} block, the combined subscriber drops; assert no further events leak.
        combined.container.sideEffectFlow.test(timeout = 500.milliseconds) {
            assertFails { awaitItem() }
        }
    }

    @Test
    fun `cancelling combined host does not cancel upstream`() = runTest {
        val a = IntHost(backgroundScope, 0)
        val b = IntHost(backgroundScope, 0)

        val combined = a.combine(b) { x, y -> x + y }

        combined.container.cancel()

        // Upstream containers should still be active and process new state updates.
        a.container.externalStateFlow.test {
            assertEquals(0, awaitItem())
            a.set(5).join()
            assertEquals(5, awaitItem())
        }
        assertTrue(a.container.scope.coroutineContext[kotlinx.coroutines.Job]!!.isActive)
    }
}
