package org.orbitmvi.orbit.internal.repeatonsubscription

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public class SubscribedCounter {
    public val subscribed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private var counter = 0
    private var job: Job? = null

    private var mutex = Mutex()

    public suspend fun increment() {
        mutex.withLock {
            if (counter == 0) {
                job?.cancel()
            }

            counter++
            subscribed.value = true
        }
    }

    public suspend fun decrement() {
        mutex.withLock {
            counter--
            if (counter == 0) {
                val newJob = GlobalScope.launch {
                    delay(MILLIS_BEFORE_IDLE)
                    mutex.withLock {
                        subscribed.value = false
                    }
                }
                job?.cancel()
                job = newJob
            }
        }
    }

    public companion object {
        private const val MILLIS_BEFORE_IDLE = 100L
    }
}
