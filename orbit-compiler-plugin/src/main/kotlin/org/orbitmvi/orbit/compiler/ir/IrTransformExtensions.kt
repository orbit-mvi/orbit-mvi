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

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

internal fun IrElement.transformIrCall(block: (IrCall) -> Unit) {
    transformChildren(
        object : IrElementTransformer<Unit> {
            override fun visitCall(expression: IrCall, data: Unit): IrElement {
                block(expression)
                return expression
            }
        },
        Unit
    )
}

internal fun IrElement.transformIrFunction(block: (IrFunction) -> Unit) {
    transformChildren(
        object : IrElementTransformer<Unit> {
            override fun visitFunction(declaration: IrFunction, data: Unit): IrStatement {
                block(declaration)
                return declaration
            }
        },
        Unit
    )
}
