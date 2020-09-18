package com.babylon.orbit2.rxjava3

import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.io.Closeable

@Suppress("DEPRECATION")
class Rx3StreamExtensionsKtTest {

    private val fixture = kotlinFixture()
    private val stream = TestStream<Int>()
    private val observable = stream.asRx3Observable().test()

    @RepeatedTest(10)
    fun `Observable receives posted values`() {
        // given a random set of values
        val values = fixture<List<Int>> {
            repeatCount { random.nextInt(1, 10) }
        }

        // when we post them to the stream
        values.forEach {
            stream.post(it)
        }

        // then the observable receives them
        assertThat(observable.values()).containsAll(values)
    }

    @Test
    fun `Observable is attached to stream`() {
        // then observable is attached to the stream
        @Suppress("UsePropertyAccessSyntax")
        assertThat(stream.hasObservers()).isTrue()
    }

    @Test
    fun `Disposing disconnects it from the stream`() {
        // when we dispose the observable
        observable.dispose()

        // then the observable is unattached from the stream
        @Suppress("UsePropertyAccessSyntax")
        assertThat(stream.hasObservers()).isFalse()
    }

    class TestStream<T> : Stream<T> {
        private val observers = mutableSetOf<(T) -> Unit>()

        override fun observe(lambda: (T) -> Unit): Closeable {
            observers += lambda
            return Closeable { observers.remove(lambda) }
        }

        fun post(value: T) = observers.forEach { it(value) }

        fun hasObservers() = observers.size > 0
    }
}
