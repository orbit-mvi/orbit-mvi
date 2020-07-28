package com.babylon.orbit2.sample.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.babylon.orbit2.ContainerHost
import com.babylon.orbit2.container
import com.babylon.orbit2.reduce
import com.babylon.orbit2.stateLiveData

class CalculatorViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val host = object : ContainerHost<CalculatorStateInternal, Nothing> {
        override val container = container<CalculatorStateInternal, Nothing>(
            CalculatorStateInternal(), savedStateHandle)
    }

    @Suppress("UNCHECKED_CAST")
    val state: LiveData<CalculatorState> = host.container.stateLiveData as LiveData<CalculatorState>

    fun clear() = host.orbit {
        reduce {
            CalculatorStateInternal()
        }
    }

    fun digit(digit: Int) = host.orbit {
        reduce {
            state.copy(xRegister = state.xRegister.appendDigit(digit))
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
            state.copy(flag = CalculatorStateInternal.Flag.Add, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun subtract() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(flag = CalculatorStateInternal.Flag.Subtract, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun multiply() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(flag = CalculatorStateInternal.Flag.Multiply, xRegister = Register(), yRegister = yRegister)
        }
    }

    fun divide() = host.orbit {
        reduce {
            val yRegister = if (state.xRegister.isEmpty()) state.yRegister else state.xRegister
            state.copy(flag = CalculatorStateInternal.Flag.Divide, xRegister = Register(), yRegister = yRegister)
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
                    val newValue = when (state.flag) {
                        CalculatorStateInternal.Flag.Add -> state.yRegister + state.xRegister
                        CalculatorStateInternal.Flag.Subtract -> state.yRegister - state.xRegister
                        CalculatorStateInternal.Flag.Divide -> state.yRegister / state.xRegister
                        CalculatorStateInternal.Flag.Multiply -> state.yRegister * state.xRegister
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
