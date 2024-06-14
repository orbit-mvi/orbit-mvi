/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.parcelize.Parcelize
import org.orbitmvi.orbit.ContainerHostWithExtState
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.reduce
import org.orbitmvi.orbit.viewmodel.container
import java.math.BigDecimal
import java.math.RoundingMode

@Suppress("TooManyFunctions")
class CalculatorViewModel(savedStateHandle: SavedStateHandle) :
    ViewModel(),
    ContainerHostWithExtState<CalculatorViewModel.InternalCalculatorState, Nothing, CalculatorState> {

    override val container = container<InternalCalculatorState, Nothing>(InternalCalculatorState(), savedStateHandle)
        .withExtState(::mapToExtState)

    private fun mapToExtState(internalState: InternalCalculatorState): CalculatorState {
        return CalculatorState(
            digitalDisplay = if (internalState.xRegister.isEmpty()) {
                internalState.yRegister.displayValue
            } else {
                internalState.xRegister.displayValue
            }
        )
    }

    // Only needed as we're using data binding and not Compose in this example
    val state: LiveData<CalculatorState> = container.extStateFlow.asLiveData()

    fun clear() = intent {
        reduce {
            InternalCalculatorState()
        }
    }

    fun digit(digit: Int) {
        intent {
            reduce {
                state.copy(xRegister = state.xRegister.appendDigit(digit))
            }
        }
    }

    fun period() = intent {
        reduce {
            state.copy(xRegister = state.xRegister.appendPeriod())
        }
    }

    fun add() = intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = InternalCalculatorState.Operator.Add, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun subtract() = intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = InternalCalculatorState.Operator.Subtract, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun multiply() = intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = InternalCalculatorState.Operator.Multiply, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun divide() = intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = InternalCalculatorState.Operator.Divide, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun plusMinus() = intent {
        reduce {
            state.copy(xRegister = state.xRegister.plusMinus())
        }
    }

    fun percentage() = intent {
        reduce {
            if (state.xRegister.isEmpty()) {
                state
            } else {
                try {
                    state.copy(xRegister = state.xRegister / Register("100"))
                } catch (ignored: Exception) {
                    state.copy(xRegister = Register("Err"))
                }
            }
        }
    }

    fun equals() = intent {
        reduce {
            try {
                if (!state.yRegister.isEmpty()) {
                    val newValue = when (state.lastOperator) {
                        InternalCalculatorState.Operator.Add -> state.yRegister + state.xRegister
                        InternalCalculatorState.Operator.Subtract -> state.yRegister - state.xRegister
                        InternalCalculatorState.Operator.Divide -> state.yRegister / state.xRegister
                        InternalCalculatorState.Operator.Multiply -> state.yRegister * state.xRegister
                        null -> state.xRegister
                    }

                    state.copy(xRegister = newValue)
                } else {
                    state
                }
            } catch (ignored: Exception) {
                state.copy(xRegister = Register("Err"))
            }
        }
    }

    @Parcelize
    data class InternalCalculatorState(
        val xRegister: Register = Register(),
        val yRegister: Register = Register(),
        val lastOperator: Operator? = null
    ) : Parcelable {

        enum class Operator {
            Add,
            Subtract,
            Divide,
            Multiply
        }
    }

    @Parcelize
    class Register(private val value: String = "") : Parcelable {
        private val asBigDecimal: BigDecimal
            get() = if (value.isEmpty()) BigDecimal.ZERO else value.toBigDecimal()

        val displayValue: String
            get() = when {
                value.isEmpty() -> "0"
                value == "-" -> "-0"
                else -> value
            }

        fun isEmpty() = value.isEmpty()

        fun appendDigit(digit: Int) =
            Register(value + digit)

        fun appendPeriod() =
            if (!value.contains('.')) Register("$value.") else this

        fun plusMinus() =
            Register(if (value.startsWith('-')) value.substring(1) else "-$value")

        operator fun plus(register: Register) =
            Register((this.asBigDecimal + register.asBigDecimal).stripTrailingZeros().toPlainString())

        operator fun minus(register: Register) =
            Register((this.asBigDecimal - register.asBigDecimal).stripTrailingZeros().toPlainString())

        operator fun div(register: Register) =
            Register((this.asBigDecimal.divide(register.asBigDecimal, SCALE, RoundingMode.HALF_EVEN)).stripTrailingZeros().toPlainString())

        operator fun times(register: Register) =
            Register((this.asBigDecimal * register.asBigDecimal).stripTrailingZeros().toPlainString())

        override fun toString() = "Register($value)"

        companion object {
            private const val SCALE = 7
        }
    }
}
