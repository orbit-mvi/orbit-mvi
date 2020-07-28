package com.babylon.orbit2.sample.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.viewmodel.container
import com.babylon.orbit2.livedata.state
import com.babylon.orbit2.reduce

class CalculatorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val host = object : ContainerHost<CalculatorStateInternal, Nothing> {
        override val container = container<CalculatorStateInternal, Nothing>(CalculatorStateInternal(), savedStateHandle)
    }

    @Suppress("UNCHECKED_CAST")
    val state: LiveData<CalculatorState> = host.container.state as LiveData<CalculatorState>

    fun clear() = host.orbit {
        reduce {
            CalculatorStateInternal()
        }
    }

    fun digit(digit: Int) {
        host.orbit {
            reduce {
                state.copy(xRegister = state.xRegister.appendDigit(digit))
            }
        }
    }

    fun period() = host.orbit {
        reduce {
            state.copy(xRegister = state.xRegister.appendPeriod())
        }
    }

    fun add() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateInternal.Operator.Add, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun subtract() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateInternal.Operator.Subtract, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun multiply() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateInternal.Operator.Multiply, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun divide() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(lastOperator = CalculatorStateInternal.Operator.Divide, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun plusMinus() = host.orbit {
        reduce {
            state.copy(xRegister = state.xRegister.plusMinus())
        }
    }

    fun percentage() = host.orbit {
        reduce {
            if (state.xRegister.isEmpty()) {
                state
            } else {
                state.copy(xRegister = state.xRegister / Register("100"))
            }
        }
    }

    fun equals() = host.orbit {
        reduce {
            try {
                if (!state.yRegister.isEmpty()) {
                    val newValue = when (state.lastOperator) {
                        CalculatorStateInternal.Operator.Add -> state.yRegister + state.xRegister
                        CalculatorStateInternal.Operator.Subtract -> state.yRegister - state.xRegister
                        CalculatorStateInternal.Operator.Divide -> state.yRegister / state.xRegister
                        CalculatorStateInternal.Operator.Multiply -> state.yRegister * state.xRegister
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
}
