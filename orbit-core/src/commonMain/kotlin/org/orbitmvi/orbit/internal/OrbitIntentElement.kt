/*
 * Copyright 2026 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit.internal

import kotlin.coroutines.CoroutineContext

/**
 * Identifies the intent a coroutine belongs to, so that reductions and side effects can be
 * attributed to it in [org.orbitmvi.orbit.observer.OrbitEventObserver] callbacks. Attached to
 * intent coroutines only when an observer is installed. Inherited by child coroutines, which
 * is what attributes work done in `subIntent`, `runOn` and `repeatOnSubscription` blocks to
 * the launching intent.
 */
internal class OrbitIntentElement(
    internal val containerId: Long,
    internal val intentId: Long,
    internal val name: String?,
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> get() = Key

    internal companion object Key : CoroutineContext.Key<OrbitIntentElement>
}
