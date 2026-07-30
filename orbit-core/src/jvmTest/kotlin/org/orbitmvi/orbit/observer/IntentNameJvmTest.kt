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

package org.orbitmvi.orbit.observer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.orbitContainer
import kotlin.test.Test
import kotlin.test.assertEquals

internal class IntentNameJvmTest {

    private val observer = RecordingObserver()

    @Test
    fun intent_names_resolve_to_host_class_and_method() = runTest {
        val host = CheckoutHost(backgroundScope, observer)
        host.loadData()
        host.container.joinIntents()

        val dispatched = observer.eventsOf<RecordedEvent.Dispatched>().single()
        assertEquals("IntentNameJvmTest.CheckoutHost.loadData", dispatched.name)
    }

    @Test
    fun lambda_class_names_parse_to_declaration_names() {
        val name = intentNameOf(makeTransformer())
        assertEquals("IntentNameJvmTest.makeTransformer", name)
    }

    @Test
    fun non_lambda_classes_fall_back_to_their_simple_name() {
        assertEquals("String", intentNameOf(""))
    }

    private fun makeTransformer(): suspend () -> Unit = {}

    private class CheckoutHost(
        scope: CoroutineScope,
        eventObserver: OrbitEventObserver?
    ) : OrbitContainerHost<Int, Int, Nothing> {
        override val container: OrbitContainer<Int, Int, Nothing> = scope.orbitContainer(
            initialState = 0,
            buildSettings = { this.eventObserver = eventObserver }
        )

        fun loadData() = intent { reduce { state + 1 } }
    }
}
