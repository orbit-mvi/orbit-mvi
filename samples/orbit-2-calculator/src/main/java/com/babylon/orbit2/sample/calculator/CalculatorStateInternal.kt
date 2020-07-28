package com.babylon.orbit2.sample.calculator

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class CalculatorStateInternal(
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
