/*
 * Copyright 2021 Mikołaj Leszczyński & Appmattus Limited
 * Copyright 2020 Babylon Partners Limited
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
 *
 * File modified by Mikołaj Leszczyński & Appmattus Limited
 * See: https://github.com/orbit-mvi/orbit-mvi/compare/c5b8b3f2b83b5972ba2ad98f73f75086a89653d3...main
 */

package org.orbitmvi.orbit.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public suspend fun assertEventually(timeout: Long = 2000L, block: suspend () -> Unit) {
    withContext(Dispatchers.Default) {
        withTimeout(timeout) {
            while (true) {
                try {
                    block()
                    break
                } catch (ignored: Throwable) {
                    yield()
                }
            }
        }
    }
}

public fun CharSequence?.assertContains(expected: CharSequence) {
    assertTrue("Does not contain $expected") {
        this?.contains(expected) ?: false
    }
}

public fun CharSequence?.assertContains(expected: Regex) {
    assertTrue("Does not contain ${expected.pattern}") {
        this?.contains(expected) ?: false
    }
}

/** Assert that a collection contains exactly the given values and nothing else, in order. */
public fun <T> Collection<T>.assertContainExactly(vararg expected: T) = assertContainExactly(expected.asList())

/** Assert that a collection contains exactly the given values and nothing else, in order. */
public fun <T, C : Collection<T>> C.assertContainExactly(expected: C) {
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

    assertTrue(passed, failureMessage())
}

/** Assert that a collection not contains exactly the given values and nothing else, in order. */
public fun <T> Collection<T>.assertNotContainExactly(vararg expected: T) {
    val actual = this

    val passed = actual.size == expected.size && actual.zip(expected).all { (a, b) -> a == b }

    assertFalse(passed, "Collection should not be exactly ${expected.asList().printed()}")
}

private fun <T, C : Collection<T>> C.printed(): String {
    val expectedPrinted = take(20).joinToString(",\n  ", prefix = "[\n  ", postfix = "\n]") { it.toString() }
    val expectedMore = if (size > 20) " ... (plus ${size - 20} more)" else ""
    return "$expectedPrinted$expectedMore"
}
