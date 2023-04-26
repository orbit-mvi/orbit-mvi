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

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.phases.CompilerContext
import org.orbitmvi.orbit.compiler.ir.processContainerHostName
import org.orbitmvi.orbit.compiler.ir.processIntentNamePass1
import org.orbitmvi.orbit.compiler.ir.processIntentNamePass2

public class ContainerHostPlugin : Meta {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> = listOf(containerHostPlugin)
}

private val Meta.containerHostPlugin: CliPlugin
    get() = "Container Host Plugin" {

        meta(
            // Container Host Name
            irProperty { irProperty ->
                processContainerHostName(irProperty)
                irProperty
            },

            // Intent Name - Pass 1
            irFunction { irFunction ->
                processIntentNamePass1(irFunction)
                irFunction
            },
            // Intent Name - Pass 2
            irFunction { irFunction ->
                processIntentNamePass2(irFunction)
                irFunction
            }
        )
    }
