/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.test

import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Assert that a collection contains exactly the given values and nothing else, in order. */
fun <T> Collection<T>.assertContainExactly(vararg expected: T) = assertContainExactly(expected.asList())

/** Assert that a collection contains exactly the given values and nothing else, in order. */
fun <T, C : Collection<T>> C.assertContainExactly(expected: C) {
    val actual = this

    val passed = actual.size == expected.size && actual.zip(expected).all { (a, b) -> a == b }

    val failureMessage = {

        val missing = expected.filterNot { actual.contains(it) }
        val extra = actual.filterNot { expected.contains(it) }

        val sb = StringBuilder()
        sb.append("Expecting: ${expected.printed()} but was: ${actual.printed()}")
        sb.append("\n")
        if (missing.isNotEmpty()) {
            sb.append("Some elements were missing: ")
            sb.append(missing.printed())
            if (extra.isNotEmpty()) {
                sb.append(" and some elements were unexpected: ")
                sb.append(extra.printed())
            }
        } else if (extra.isNotEmpty()) {
            sb.append("Some elements were unexpected: ")
            sb.append(extra.printed())
        }
        sb.toString()
    }

    // "Collection should not be exactly ${expected.printed().value}"

    assertTrue(passed, failureMessage())
}


/** Assert that a collection not contains exactly the given values and nothing else, in order. */
fun <T> Collection<T>.assertNotContainExactly(vararg expected: T) = assertNotContainExactly(expected.asList())

/** Assert that a collection not contains exactly the given values and nothing else, in order. */
fun <T, C : Collection<T>> C.assertNotContainExactly(expected: C) {
    val actual = this

    val passed = actual.size == expected.size && actual.zip(expected).all { (a, b) -> a == b }

    assertFalse(passed, "Collection should not be exactly ${expected.printed()}")
}

private fun <T, C : Collection<T>> C.printed(): String {
    val expectedPrinted = take(20).joinToString(",\n  ", prefix = "[\n  ", postfix = "\n]") { it.toString() }
    val expectedMore = if (size > 20) " ... (plus ${size - 20} more)" else ""
    return "$expectedPrinted$expectedMore"
}
