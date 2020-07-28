package com.babylon.orbit2.sample.calculator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.RealContainer
import com.babylon.orbit2.sample.calculator.livedata.MockLifecycleOwner
import com.babylon.orbit2.sample.calculator.livedata.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class CalculatorViewModelTest {

    private val viewModel = CalculatorViewModel(SavedStateHandle())

    init {
        val host = CalculatorViewModel::class.java.getDeclaredField("host").apply { isAccessible = true }.get(viewModel) as ContainerHost<*, *>

        val realContainer =
            host.container::class.java.getDeclaredField("actual").apply { isAccessible = true }.get(host.container)

        RealContainer::class.java.getDeclaredField("scope").apply { isAccessible = true }
            .set(realContainer, CoroutineScope(Dispatchers.Unconfined))
    }

    private val mockLifecycleOwner = MockLifecycleOwner().also {
        it.dispatchEvent(Lifecycle.Event.ON_CREATE)
        it.dispatchEvent(Lifecycle.Event.ON_START)
    }

    @Test
    fun `no value displays 0`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        assertEquals("0", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `negative value displays -0`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.plusMinus()

        assertEquals("-0", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `1 add 1 equals 2`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(1)
        viewModel.add()
        viewModel.digit(1)
        viewModel.equals()

        assertEquals("2", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `2 add 3 equals 5`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(2)
        viewModel.add()
        viewModel.digit(3)
        viewModel.equals()

        assertEquals("5", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `2 add -3 equals -1`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(2)
        viewModel.add()
        viewModel.digit(3)
        viewModel.plusMinus()
        viewModel.equals()

        assertEquals("-1", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `9 divide 2 equals 4_5`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(9)
        viewModel.divide()
        viewModel.digit(2)
        viewModel.equals()

        assertEquals("4.5", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `5 multiply 5 equals 25`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(5)
        viewModel.multiply()
        viewModel.digit(5)
        viewModel.equals()

        assertEquals("25", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `99 percentage is 0_99`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(9)
        viewModel.digit(9)
        viewModel.percentage()

        assertEquals("0.99", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `9_9 subtract 0_4 equals 9_5`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(9)
        viewModel.period()
        viewModel.digit(9)
        viewModel.subtract()
        viewModel.period()
        viewModel.digit(4)
        viewModel.equals()

        assertEquals("9.5", testLiveData.values.last().digitalDisplay)
    }

    @Test
    fun `value cleared`() {
        val testLiveData = viewModel.state.test(mockLifecycleOwner)

        viewModel.digit(9)
        viewModel.multiply()
        viewModel.digit(9)
        viewModel.clear()

        assertEquals("0", testLiveData.values.last().digitalDisplay)
    }
}
