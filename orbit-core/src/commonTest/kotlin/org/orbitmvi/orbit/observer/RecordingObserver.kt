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

import kotlin.concurrent.atomics.AtomicReference

/**
 * Thread-safe [OrbitEventObserver] test fixture recording every callback.
 */
internal class RecordingObserver : OrbitEventObserver {

    private val recorded = AtomicReference<List<RecordedEvent>>(emptyList())

    val events: List<RecordedEvent> get() = recorded.load()

    inline fun <reified T : RecordedEvent> eventsOf(): List<T> = events.filterIsInstance<T>()

    override fun onContainerCreated(containerInfo: ContainerInfo) {
        append(RecordedEvent.Created(containerInfo.containerId, containerInfo.containerName, containerInfo.initialState))
    }

    override fun onContainerClosed(containerId: Long) {
        append(RecordedEvent.Closed(containerId))
    }

    override fun onIntentDispatched(containerId: Long, intentId: Long, name: String?) {
        append(RecordedEvent.Dispatched(containerId, intentId, name))
    }

    override fun onIntentCompleted(containerId: Long, intentId: Long, result: IntentResult) {
        append(RecordedEvent.Completed(containerId, intentId, result))
    }

    override fun onReduction(containerId: Long, intentId: Long?, oldState: Any, newState: Any) {
        append(RecordedEvent.Reduction(containerId, intentId, oldState, newState))
    }

    override fun onSideEffect(containerId: Long, intentId: Long?, sideEffect: Any) {
        append(RecordedEvent.SideEffect(containerId, intentId, sideEffect))
    }

    private fun append(event: RecordedEvent) {
        while (true) {
            val current = recorded.load()
            if (recorded.compareAndSet(current, current + event)) return
        }
    }
}

internal sealed interface RecordedEvent {
    data class Created(val containerId: Long, val containerName: String?, val initialState: Any) : RecordedEvent
    data class Closed(val containerId: Long) : RecordedEvent
    data class Dispatched(val containerId: Long, val intentId: Long, val name: String?) : RecordedEvent
    data class Completed(val containerId: Long, val intentId: Long, val result: IntentResult) : RecordedEvent
    data class Reduction(val containerId: Long, val intentId: Long?, val oldState: Any, val newState: Any) : RecordedEvent
    data class SideEffect(val containerId: Long, val intentId: Long?, val sideEffect: Any) : RecordedEvent
}
