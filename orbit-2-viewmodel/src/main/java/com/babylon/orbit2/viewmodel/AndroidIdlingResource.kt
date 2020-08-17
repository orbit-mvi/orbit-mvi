package com.babylon.orbit2.viewmodel

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource.ResourceCallback
import com.babylon.orbit2.idling.IdlingResource
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class AndroidIdlingResource : IdlingResource {

    private val counter: AtomicInteger = AtomicInteger(0)
    private val idle = AtomicBoolean(true)

    private val job = AtomicReference<Job>()

    private var resourceCallback: ResourceCallback? = null

    private val espressoIdlingResource = object : androidx.test.espresso.IdlingResource {
        private val uniqueId = UUID.randomUUID()
        override fun getName() = "orbit-mvi-$uniqueId"

        override fun isIdleNow() = idle.get()

        override fun registerIdleTransitionCallback(resourceCallback: ResourceCallback?) {
            this@AndroidIdlingResource.resourceCallback = resourceCallback
        }
    }

    init {
        IdlingRegistry.getInstance().register(espressoIdlingResource)
    }

    override fun increment() {
        if (counter.getAndIncrement() == 0) {
            job.get()?.cancel()
        }
        idle.set(false)
    }

    override fun decrement() {
        if (counter.decrementAndGet() == 0) {
            job.getAndSet(GlobalScope.launch {
                delay(MILLIS_BEFORE_IDLE)
                idle.set(true)
                resourceCallback?.onTransitionToIdle()
            })?.cancel()
        }
    }

    override fun close() {
        IdlingRegistry.getInstance().unregister(espressoIdlingResource)
    }

    companion object {
        private const val MILLIS_BEFORE_IDLE = 100L
    }
}
