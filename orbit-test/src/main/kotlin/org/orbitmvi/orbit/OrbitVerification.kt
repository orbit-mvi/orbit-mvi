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

package org.orbitmvi.orbit

public class OrbitVerification<STATE : Any, SIDE_EFFECT : Any> {
    internal var expectedSideEffects = emptyList<SIDE_EFFECT>()
    internal var expectedStateChanges = emptyList<STATE.() -> STATE>()

    /**
     * Assert that the expected sequence of state changes has been emitted.
     *
     * The initial state is asserted automatically, only assert further states.
     *
     * Every assertion is a lambda with the previous state as the receiver. It is therefore
     * recommended to use `copy` to make your tests more concise and readable.
     *
     * We are more interested in how the state _changed_ from the previous one, rather than what
     * it currently is.
     *
     * ``` kotlin
     * testSubject.assert {
     *     states(
     *         { copy(count = 2) },
     *         { copy(count = 4) },
     *         { copy(finished = true) }
     *     )
     * }
     * ```
     *
     * @param expectedStateChanges A list of expected state _changes_. Each lambda has the
     * previous state as the receiver.
     */
    public fun states(vararg expectedStateChanges: STATE.() -> STATE) {
        this.expectedStateChanges = expectedStateChanges.toList()
    }

    /**
     * Assert that the expected side effects have been posted.
     *
     * @param expectedSideEffects Expected side effects.
     */
    public fun postedSideEffects(vararg expectedSideEffects: SIDE_EFFECT) {
        this.expectedSideEffects = expectedSideEffects.toList()
    }

    /**
     * Assert that the expected side effects have been posted.
     *
     * @param expectedSideEffects Expected side effects.
     */
    public fun postedSideEffects(expectedSideEffects: Iterable<SIDE_EFFECT>) {
        this.expectedSideEffects = expectedSideEffects.toList()
    }
}
