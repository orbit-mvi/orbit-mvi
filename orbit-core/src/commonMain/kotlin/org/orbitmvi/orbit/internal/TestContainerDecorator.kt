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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerDecorator
import org.orbitmvi.orbit.internal.repeatonsubscription.SubscribedCounter
import org.orbitmvi.orbit.internal.repeatonsubscription.Subscription
import org.orbitmvi.orbit.syntax.ContainerContext

public class TestContainerDecorator<STATE : Any, SIDE_EFFECT : Any>(
    private val parentScope: CoroutineScope,
    override val actual: Container<STATE, SIDE_EFFECT>
) : ContainerDecorator<STATE, SIDE_EFFECT> {

    private val delegate = atomic(actual)

    public val savedIntents: Channel<(suspend () -> Unit)>
        get() = (delegate.value as? InterceptingContainerDecorator)?.savedIntents ?: Channel()

    override val settings: Container.Settings
        get() = delegate.value.settings

    override val stateFlow: StateFlow<STATE>
        get() = delegate.value.stateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = delegate.value.sideEffectFlow

    override suspend fun orbit(orbitIntent: suspend ContainerContext<STATE, SIDE_EFFECT>.() -> Unit) {
        delegate.value.orbit(orbitIntent)
    }

    public fun test(
        initialState: STATE,
        strategy: TestingStrategy
    ) {
        val testDelegate = RealContainer<STATE, SIDE_EFFECT>(
            initialState = initialState,
            parentScope = parentScope,
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
            throw IllegalStateException("Can only call test() once")
        }
    }
}

private object AlwaysSubscribedCounter: SubscribedCounter {
    override val subscribed: Flow<Subscription> = flowOf(Subscription.Subscribed)

    override suspend fun increment() = Unit

    override suspend fun decrement() = Unit
}
