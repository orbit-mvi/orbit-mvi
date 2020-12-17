/*
 * Copyright 2020 Babylon Partners Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.babylon.orbit2.sample.calculator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import com.appmattus.kotlinfixture.kotlinFixture
import com.babylon.orbit2.sample.calculator.livedata.InstantTaskExecutorExtension
import com.babylon.orbit2.sample.calculator.livedata.MockLifecycleOwner
import com.babylon.orbit2.sample.calculator.livedata.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.AfterEach
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
import java.util.stream.Stream

@ExperimentalCoroutinesApi
@ExtendWith(InstantTaskExecutorExtension::class)
class CalculatorViewModelTest {

    private val viewModel by lazy { CalculatorViewModel(SavedStateHandle()) }

    private val mockLifecycleOwner = MockLifecycleOwner()

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockLifecycleOwner.let {
            it.dispatchEvent(Lifecycle.Event.ON_CREATE)
            it.dispatchEvent(Lifecycle.Event.ON_START)
        }
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    /**
     * Enter the whole number [value] into [this]
     * @return Number of characters entered
     */
    private fun CalculatorViewModel.enterNumber(value: Int): Int = enterNumber(value.toDouble())

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
    fun `empty initial value displays as '0'`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        testLiveData.awaitCount(1)

        assertEquals("0", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `negated empty initial value displays as '-0'`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.plusMinus()

        testLiveData.awaitCount(2)

        assertEquals("-0", testLiveData.values.last().digitalDisplay)
    }

    @ParameterizedTest(name = "{0} + {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `add decimal numbers`(a: Double, b: Double) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.add()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a + b, testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
    }

    @ParameterizedTest(name = "{0} + {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `add whole numbers`(a: Int, b: Int) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.add()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a + b, testLiveData.values.last().digitalDisplay.toInt())
    }

    @ParameterizedTest(name = "{0} − {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `subtract decimal numbers`(a: Double, b: Double) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.subtract()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a - b, testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
    }

    @ParameterizedTest(name = "{0} − {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `subtract whole numbers`(a: Int, b: Int) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.subtract()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a - b, testLiveData.values.last().digitalDisplay.toInt())
    }

    @ParameterizedTest(name = "{0} × {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `multiply decimal numbers`(a: Double, b: Double) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.multiply()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a * b, testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
    }

    @ParameterizedTest(name = "{0} × {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `multiply whole numbers`(a: Int, b: Int) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.multiply()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a * b, testLiveData.values.last().digitalDisplay.toInt())
    }

    @ParameterizedTest(name = "{0} ÷ {1}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `divide decimal numbers`(a: Double, b: Double) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.divide()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        assertEquals(a / b, testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
    }

    @ParameterizedTest(name = "{0} ÷ {1}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `divide whole numbers`(a: Int, b: Int) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.divide()
        val bCount = viewModel.enterNumber(b)
        viewModel.equals()

        testLiveData.awaitCount(aCount + bCount + 3)

        if (b == 0) {
            assertEquals("Err", testLiveData.values.last().digitalDisplay)
        } else {
            assertEquals(a.toDouble() / b.toDouble(), testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
        }
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(DecimalNumberPairProvider::class)
    fun `percentage decimal number`(a: Double) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.percentage()

        testLiveData.awaitCount(aCount + 2)

        assertEquals(a / 100, testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
    }

    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(WholeNumberPairProvider::class)
    fun `percentage whole number`(a: Int) {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        val aCount = viewModel.enterNumber(a)
        viewModel.percentage()

        testLiveData.awaitCount(aCount + 2)

        assertEquals(a.toDouble() / 100, testLiveData.values.last().digitalDisplay.toDouble(), 0.00001)
    }

    @RepeatedTest(10)
    fun `clears values`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        // Press some random buttons
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

        viewModel.clear()

        testLiveData.awaitIdle()

        assertEquals("0", testLiveData.values.last().digitalDisplay)
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
