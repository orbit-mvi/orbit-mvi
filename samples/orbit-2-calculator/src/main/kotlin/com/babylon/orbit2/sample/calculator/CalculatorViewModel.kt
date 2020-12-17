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

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.syntax.simple.intent
import com.babylon.orbit2.syntax.simple.reduce
import com.babylon.orbit2.viewmodel.container
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

class CalculatorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    // private ContainerHost to not expose the caller to the internal implementation of CalculatorState
    private val host = object : ContainerHost<CalculatorStateImpl, Nothing> {
        override var container = container<CalculatorStateImpl, Nothing>(CalculatorStateImpl(), savedStateHandle)
    }

    @Suppress("UNCHECKED_CAST")
    val state: LiveData<CalculatorState> = host.container.stateFlow.asLiveData() as LiveData<CalculatorState>

    fun clear() = host.intent {
        reduce {
            CalculatorStateImpl()
        }
    }

    fun digit(digit: Int) {
        host.intent {
            reduce {
                state.copy(xRegister = state.xRegister.appendDigit(digit))
            }
        }
    }

    fun period() = host.intent {
        reduce {
            state.copy(xRegister = state.xRegister.appendPeriod())
        }
    }

    fun add() = host.intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateImpl.Operator.Add, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun subtract() = host.intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateImpl.Operator.Subtract, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun multiply() = host.intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateImpl.Operator.Multiply, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun divide() = host.intent {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateImpl.Operator.Divide, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun plusMinus() = host.intent {
        reduce {
            state.copy(xRegister = state.xRegister.plusMinus())
        }
    }

    fun percentage() = host.intent {
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

    fun equals() = host.intent {
        reduce {
            try {
                if (!state.yRegister.isEmpty()) {
                    val newValue = when (state.lastOperator) {
                        CalculatorStateImpl.Operator.Add -> state.yRegister + state.xRegister
                        CalculatorStateImpl.Operator.Subtract -> state.yRegister - state.xRegister
                        CalculatorStateImpl.Operator.Divide -> state.yRegister / state.xRegister
                        CalculatorStateImpl.Operator.Multiply -> state.yRegister * state.xRegister
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
    private data class CalculatorStateImpl(
        val xRegister: Register = Register(),
        val yRegister: Register = Register(),
        val lastOperator: Operator? = null
    ) : CalculatorState, Parcelable {

        override val digitalDisplay: String
            get() = if (xRegister.isEmpty()) yRegister.displayValue else xRegister.displayValue

        enum class Operator {
            Add,
            Subtract,
            Divide,
            Multiply
        }
    }

    @Parcelize
    private class Register(private val value: String = "") : Parcelable {
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
