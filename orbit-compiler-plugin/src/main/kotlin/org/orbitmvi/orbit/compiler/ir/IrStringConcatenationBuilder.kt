/*
 * Copyright 2023 Mikołaj Leszczyński & Appmattus Limited
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
 */

package org.orbitmvi.orbit.compiler.ir

import arrow.meta.phases.codegen.ir.IrUtils
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrStringConcatenationImpl
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET

internal class IrStringConcatenationBuilder(private val irUtils: IrUtils, private val irStringConcatenation: IrStringConcatenation) {
    fun append(value: String) {
        irStringConcatenation.addArgument(irUtils.irString(value))
    }

    fun append(value: IrValueParameter) {
        irStringConcatenation.addArgument(irUtils.irGet(value))
    }
}

internal fun IrUtils.buildIrStringConcatenation(block: IrStringConcatenationBuilder.() -> Unit): IrStringConcatenation {
    val string = IrStringConcatenationImpl(
        SYNTHETIC_OFFSET,
        SYNTHETIC_OFFSET,
        pluginContext.irBuiltIns.stringType
    )
    block(IrStringConcatenationBuilder(this, string))
    return string
}
