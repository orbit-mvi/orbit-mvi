package com.babylon.orbit2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
internal class FlowToStreamThreadingTest {

    @Nested
    inner class Default {
        @Test
        fun `stream observed on default dispatcher if no main is installed`() {
            val channel = Channel<Int>()
            val latch = CountDownLatch(1)
            var threadName = ""

            channel.receiveAsFlow().asStream().observe {
                threadName = Thread.currentThread().name
                latch.countDown()
            }

            channel.sendBlocking(123)

            latch.await(5, TimeUnit.SECONDS)

            assertThat(threadName).startsWith("Default")
        }
    }

    @ObsoleteCoroutinesApi
    @Nested
    inner class Main {

        @BeforeEach
        fun beforeEach() {
            Dispatchers.setMain(
                newSingleThreadContext("main")
            )
        }

        @AfterEach
        fun afterEach() {
            Dispatchers.resetMain()
        }

        @Test
        fun `stream observed on main dispatcher if installed`() {
            val channel = Channel<Int>()
            val latch = CountDownLatch(1)
            var threadName = ""

            channel.receiveAsFlow().asStream().observe {
                threadName = Thread.currentThread().name
                latch.countDown()
            }

            channel.sendBlocking(123)

            latch.await(5, TimeUnit.SECONDS)

            assertThat(threadName).startsWith("main")
        }
    }
}
