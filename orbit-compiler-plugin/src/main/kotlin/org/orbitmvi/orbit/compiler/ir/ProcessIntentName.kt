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
import org.jetbrains.kotlin.backend.common.lower.parents
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.util.allParameters
import org.jetbrains.kotlin.ir.util.getArgumentsWithIr
import org.jetbrains.kotlin.ir.util.kotlinFqName

private const val INTENT_CALL_MAPPING = "Orbit-Compiler-Intent-Call-Mapping"

private data class IntentCall(val parent: IrFunction, val call: IrCall, val depth: Int)

/**
 * processIntentNamePass1 looks for [IrCall] intent blocks in [IrFunction]. It stores the pairing in the
 * compiler context for the deepest [IrFunction] (i.e. the function closest to the call site)
 *
 * We perform two passes as we make no assumptions about the order this call
 */
internal fun IrUtils.processIntentNamePass1(irFunction: IrFunction) {
    val intentCallMap = compilerContext.get<MutableMap<IrCall, IntentCall>>(INTENT_CALL_MAPPING) ?: mutableMapOf()

    irFunction.transformIrCall { irCall ->
        if (irCall.symbol.owner.kotlinFqName.asString() == "org.orbitmvi.orbit.syntax.simple.intent") {
            val depth = irFunction.parents.count()

            val currentEntry = intentCallMap[irCall]
            if (currentEntry == null || depth > currentEntry.depth) {
                intentCallMap[irCall] = IntentCall(irFunction, irCall, depth)
            }
        }
    }

    compilerContext.set(INTENT_CALL_MAPPING, intentCallMap)
}

internal fun IrUtils.processIntentNamePass2(irFunction: IrFunction) {
    val intentCallMap = compilerContext.get<Map<IrCall, IntentCall>>(INTENT_CALL_MAPPING) ?: emptyMap()
    val intentCalls = intentCallMap.values.mapNotNull { intentCall -> intentCall.call.takeIf { intentCall.parent == irFunction } }

    if (intentCalls.isNotEmpty()) {
        irFunction.transformIrCall { irCall ->
            if (intentCalls.contains(irCall)) {
                val intentFunctionName = intentFunctionName(irFunction)

                val intentNameValueParameter =
                    irCall.symbol.owner.allParameters.first { it.name.asString() == "intentName" }

                val arguments = irCall.getArgumentsWithIr()
                val hasIntentNameOverride = arguments.any { it.first == intentNameValueParameter }

                if (!hasIntentNameOverride) {
                    val str = buildIrStringConcatenation {
                        append(intentFunctionName)
                        append("(")

                        irFunction.valueParameters.forEachIndexed { index, parameter ->
                            append(parameter.name.asString())
                            append(" = ")
                            append(parameter)

                            if (index != irFunction.valueParameters.size - 1) {
                                append(", ")
                            }
                        }

                        append(")")
                    }

                    irCall.putArgument(intentNameValueParameter, str)
                }
            }
        }
    }
}

private fun intentFunctionName(irFunction: IrFunction): String {
    val hierarchy = listOf(irFunction) + irFunction.parents
    val hier = hierarchy.mapIndexed { index, parent ->
        var name = if (index == hierarchy.size - 1) parent.kotlinFqName.asString() else parent.kotlinFqName.shortName().asString()
        if (parent is IrFunction) {
            if (parent.name.asString() == "<anonymous>") {
                var functionIndex = 0
                parent.parent.transformIrFunction { declaration ->
                    // only count functions that are a direct descendant
                    if (declaration.parent == parent.parent) functionIndex++

                    if (declaration == parent) {
                        name = "\$$functionIndex"
                    }
                }
            }
        }

        name
    }

    // intentFunctionName
    return hier.reversed().filterNot { it.isBlank() }.joinToString(".")
}
