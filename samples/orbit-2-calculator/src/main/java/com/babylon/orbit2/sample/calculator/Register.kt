package com.babylon.orbit2.sample.calculator

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.RoundingMode

@Parcelize
class Register(private val value: String = "") : Parcelable {
    private val asBigDecimal: BigDecimal
        get() = if (value.isEmpty()) BigDecimal.ZERO else value.toBigDecimal()

    val displayValue: String
        get() = if (value.isEmpty()) "0" else value

    fun isEmpty() = value.isEmpty()

    fun appendDigit(digit: Int) =
        Register(value + digit)

    fun appendPeriod() =
        if (!value.contains('.')) Register("$value.") else this

    fun plusMinus() =
        Register(if (value.startsWith('-')) value.substring(1) else "-$value")

    operator fun plus(register: Register) =
        Register(
            (this.asBigDecimal + register.asBigDecimal).stripTrailingZeros().toPlainString()
        )

    operator fun minus(register: Register) =
        Register(
            (this.asBigDecimal - register.asBigDecimal).stripTrailingZeros().toPlainString()
        )

    operator fun div(register: Register) =
        Register(
            (this.asBigDecimal.divide(
                register.asBigDecimal,
                SCALE,
                RoundingMode.HALF_EVEN
            )).stripTrailingZeros().toPlainString()
        )

    operator fun times(register: Register) =
        Register(
            (this.asBigDecimal * register.asBigDecimal).stripTrailingZeros().toPlainString()
        )

    override fun toString() = "Register($value)"

    companion object {
        private const val SCALE = 7
    }
}
