/*
 * Copyright 2021-2025 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.sample.calculator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import com.appmattus.kotlinfixture.kotlinFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.orbitmvi.orbit.sample.calculator.CalculatorViewModel.InternalCalculatorState
import org.orbitmvi.orbit.sample.calculator.livedata.InstantTaskExecutorExtension
import org.orbitmvi.orbit.sample.calculator.livedata.MockLifecycleOwner
import org.orbitmvi.orbit.test.OrbitTestContextWithExternalState
import org.orbitmvi.orbit.test.TestSettings
import org.orbitmvi.orbit.test.test
import java.util.stream.Stream

@ExtendWith(InstantTaskExecutorExtension::class)
class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    private val mockLifecycleOwner = MockLifecycleOwner()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockLifecycleOwner.let {
            it.dispatchEvent(Lifecycle.Event.ON_CREATE)
            it.dispatchEvent(Lifecycle.Event.ON_START)
        }
        viewModel = CalculatorViewModel(SavedStateHandle())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    /**
     * Enter the whole number [value] into [this]
     * @return Number of characters entered
     */
    private fun CalculatorViewModel.enterNumber(value: Int): Int {
        return value.toString().apply {
            forEach {
                when (it) {
                    '-' -> plusMinus()
                    '.' -> period()
                    else -> digit(it.toString().toInt())
                }
            }
        }.length
    }

    /**
     * Enter the decimal number [value] into [this]
     * @return Number of characters entered
     */
    private fun CalculatorViewModel.enterNumber(value: Double): Int {
        return value.toString().apply {
            forEach {
                when (it) {
                    '-' -> plusMinus()
                    '.' -> period()
                    else -> digit(it.toString().toInt())
                }
            }
        }.length
    }

    @Test
    fun `empty initial value displays as '0'`() = runTest {
        viewModel.test(this, settings = TestSettings(autoCheckInitialState = false)) {
            assertEquals("0", awaitExternalState().digitalDisplay)
        }
    }

    @Test
    fun `negated empty initial value displays as '-0'`() = runTest {
        viewModel.test(this) {
            viewModel.plusMinus()

            assertEquals("-0", awaitExternalState().digitalDisplay)
        }
    }

    @ParameterizedTest(name = "{0} + {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `add decimal numbers`(a: Double, b: Double) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.add()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            assertEquals(a + b, awaitExternalState().digitalDisplay.toDouble(), 0.00001)
        }
    }

    @ParameterizedTest(name = "{0} + {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `add whole numbers`(a: Int, b: Int) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.add()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            // The display only updates if the last value and the result of the calculation are different
            if (a + b != b) {
                assertEquals(a + b, awaitExternalState().digitalDisplay.toInt())
            }
        }
    }

    @ParameterizedTest(name = "{0} − {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `subtract decimal numbers`(a: Double, b: Double) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.subtract()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            assertEquals(a - b, awaitExternalState().digitalDisplay.toDouble(), 0.00001)
        }
    }

    @ParameterizedTest(name = "{0} − {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `subtract whole numbers`(a: Int, b: Int) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.subtract()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            assertEquals(a - b, awaitExternalState().digitalDisplay.toInt())
        }
    }

    private suspend fun OrbitTestContextWithExternalState<InternalCalculatorState, CalculatorState, Nothing, *>.awaitEntry(value: Number) {
        val currentValue = containerHost.container.externalStateFlow.value.digitalDisplay

        // If the value to enter is a single digit and the current value is the same digit then the digital display will not update
        if (value.toString().length == 1 && value.toString() == currentValue) {
            return
        }

        @Suppress("ControlFlowWithEmptyBody")
        while (value.toString() != awaitExternalState().digitalDisplay) {
        }
    }

    @ParameterizedTest(name = "{0} × {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `multiply decimal numbers`(a: Double, b: Double) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.multiply()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            assertEquals(a * b, awaitExternalState().digitalDisplay.toDouble(), 0.00001)
        }
    }

    @ParameterizedTest(name = "{0} × {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `multiply whole numbers`(a: Int, b: Int) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.multiply()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            // The display only updates if the last value and the result of the calculation are different
            if (a * b != b) {
                assertEquals(a * b, awaitExternalState().digitalDisplay.toInt())
            }
        }
    }

    @ParameterizedTest(name = "{0} ÷ {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `divide decimal numbers`(a: Double, b: Double) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.divide()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            if (b == 0.0) {
                assertEquals("Err", awaitExternalState().digitalDisplay)
            } else if ((a / b) - b >= 0.00001 || (a / b) - b <= -0.00001 || (a / b).toString().endsWith(".0")) {
                // The display only updates if the last value and the result of the calculation are different
                assertEquals(a / b, awaitExternalState().digitalDisplay.toDouble(), 0.00001)
            }
        }
    }

    @ParameterizedTest(name = "{0} ÷ {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `divide whole numbers`(a: Int, b: Int) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.divide()

            viewModel.enterNumber(b)
            awaitEntry(b)

            viewModel.equals()

            if (b == 0) {
                assertEquals("Err", awaitExternalState().digitalDisplay)
            } else if ((a.toDouble() / b.toDouble()) - b.toDouble() >= 0.00001 || (a.toDouble() / b.toDouble()) - b.toDouble() <= -0.00001) {
                // The display only updates if the last value and the result of the calculation are different
                assertEquals(a.toDouble() / b.toDouble(), awaitExternalState().digitalDisplay.toDouble(), 0.00001)
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `percentage decimal number`(a: Double) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.percentage()

            // The display only updates if the last value is not zero
            if (a != 0.0) {
                assertEquals(a / 100, awaitExternalState().digitalDisplay.toDouble(), 0.00001)
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `percentage whole number`(a: Int) = runTest {
        viewModel.test(this) {
            viewModel.enterNumber(a)
            awaitEntry(a)

            viewModel.percentage()

            // The display only updates if the last value is not zero
            if (a != 0) {
                assertEquals(a.toDouble() / 100, awaitExternalState().digitalDisplay.toDouble(), 0.00001)
            }
        }
    }

    @RepeatedTest(10)
    fun `clears values`() = runTest {
        viewModel.test(this) {
            repeat(fixture(0..15)) {
                when (fixture(0..8)) {
                    0 -> viewModel.digit(fixture(0..9))
                    1 -> viewModel.equals()
                    2 -> viewModel.multiply()
                    3 -> viewModel.divide()
                    4 -> viewModel.subtract()
                    5 -> viewModel.add()
                    6 -> viewModel.percentage()
                    7 -> viewModel.period()
                    8 -> viewModel.plusMinus()
                }
            }

            viewModel.add()
            viewModel.digit(9)
            viewModel.digit(9)
            viewModel.digit(9)

            @Suppress("ControlFlowWithEmptyBody")
            while (awaitExternalState().digitalDisplay != "999") {
            }

            viewModel.clear()

            assertEquals("0", awaitExternalState().digitalDisplay)
        }
    }

    class DecimalNumberPairProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return List(10) {
                arguments(fixture(-10000..10000) / 100f, fixture(-10000..10000) / 100f)
            }.stream()
        }
    }

    class WholeNumberPairProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> {
            return List(10) {
                arguments(fixture(-100..100), fixture(-100..100))
            }.stream()
        }
    }

    companion object {
        private val fixture = kotlinFixture()
    }
}
