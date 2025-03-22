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

package org.orbitmvi.orbit.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import org.orbitmvi.orbit.Container

public data class TestSettings(
    /**
     * Set this to override the [Container]'s [CoroutineDispatcher]s for this test
     */
    val dispatcherOverride: CoroutineDispatcher? = null,
    /**
     * Set this to override the [Container]'s [CoroutineExceptionHandler]s for this test
     */
    val exceptionHandlerOverride: CoroutineExceptionHandler? = null,

    val implicitInitialState: Boolean = true
)
