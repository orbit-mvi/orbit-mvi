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

package org.orbitmvi.orbit.compiler

import arrow.meta.phases.codegen.ir.IrUtils
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.util.getArgumentsWithIr
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.parentClassOrNull
import org.orbitmvi.orbit.compiler.ir.irString
import org.orbitmvi.orbit.compiler.ir.returnTypeFqName
import org.orbitmvi.orbit.compiler.ir.transformIrCall

/**
 * processContainerHostName takes an [IrProperty] that represents the container property of a `ContainerHost` and
 * adds `containerHostName` to the container [IrCall] if not already present
 */
internal fun IrUtils.processContainerHostName(irProperty: IrProperty) {
    if (irProperty.name.asString() == "container" && irProperty.returnTypeFqName?.toString() == "org.orbitmvi.orbit.Container") {
        val containerHostName = irProperty.parentClassOrNull?.kotlinFqName?.toString() ?: "<unknown>"

        irProperty.transformIrCall { irCall ->
            if (irCall.symbol.owner.kotlinFqName.asString() == "org.orbitmvi.orbit.container") {
                val containerHostNameValueParameter =
                    irCall.symbol.owner.valueParameters.first { it.name.asString() == "containerHostName" }

                val arguments = irCall.getArgumentsWithIr()
                val hasContainerHostNameOverride = arguments.any { it.first == containerHostNameValueParameter }
                if (!hasContainerHostNameOverride) {
                    irCall.putArgument(containerHostNameValueParameter, irString(containerHostName))
                }
            }
        }
    }
}
