/*
 * Copyright 2021 Mikolaj Leszczynski & Matthew Dolan
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

package org.orbitmvi.orbit.syntax.strict

import org.orbitmvi.orbit.syntax.Operator
import org.orbitmvi.orbit.syntax.OrbitDsl

/**
 * Represents the current context in which an [Operator] is executing with access to the [volatileState].
 */
@OrbitDsl
public interface VolatileContext<STATE : Any, EVENT> : Context<STATE, EVENT> {
    /**
     * The current state which can change throughout execution of the operator
     */
    public fun volatileState(): STATE
}
