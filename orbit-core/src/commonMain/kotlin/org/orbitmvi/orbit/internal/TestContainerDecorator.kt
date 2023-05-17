/*
 * Copyright 2021-2022 Mikołaj Leszczyński & Appmattus Limited
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

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.RealSettings
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.syntax.ContainerContext

public class TestContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    public val originalInitialState: STATE,
    private val parentScope: CoroutineScope,
    override val actual: Container<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {

    private val delegate = atomic(actual)

    public val savedIntents: Channel<(suspend () -> Unit)>
        get() = (delegate.value as? InterceptingContainerDecorator)?.savedIntents ?: Channel()

    override val settings: RealSettings
        get() = delegate.value.settings

    override val stateFlow: StateFlow<STATE>
        get() = delegate.value.stateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.value.sideEffectFlow

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit): Job {
        return delegate.value.orbit(orbitIntent)
    }

    @OptIn(OrbitExperimental::class)
    override suspend fun inlineOrbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.value.inlineOrbit(orbitIntent)
    }

    public fun test(
        initialState: STATE? = null,
        strategy: TestingStrategy,
        testScope: CoroutineScope?
    ) {
        val testDelegate = RealContainer<STATE, SIDE_EFFECT>(
            initialState = initialState ?: originalInitialState,
            parentScope = testScope ?: parentScope,
            settings = strategy.settings,
            subscribedCounterOverride = AlwaysSubscribedCounter
        ).let {
            if (strategy is TestingStrategy.Suspending) {
                InterceptingContainerDecorator(it)
            } else {
                it
            }
        }

        val testDelegateSet = delegate.compareAndSet(
            expect = actual,
            update = testDelegate
        )

        if (!testDelegateSet) {
            error("Can only call test() once")
        }
    }

    @OptIn(OrbitExperimental::class)
    public override suspend fun joinIntents() {
        delegate.value.joinIntents()
    }

    @OptIn(OrbitExperimental::class)
    public override fun cancel() {
        delegate.value.cancel()
    }
}
