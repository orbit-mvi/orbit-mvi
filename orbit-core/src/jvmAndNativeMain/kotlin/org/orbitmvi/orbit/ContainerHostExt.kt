/*
 * Copyright 2025 Mikołaj Leszczyński & Appmattus Limited
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

package org.orbitmvi.orbit

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.orbitmvi.orbit.annotation.OrbitDsl
import org.orbitmvi.orbit.annotation.OrbitExperimental
import org.orbitmvi.orbit.idling.withIdling
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.syntax.intent
import kotlin.coroutines.cancellation.CancellationException

/**
 * Build and execute an intent on [Container] in a blocking manner, without dispatching.
 *
 * This API is reserved for special cases e.g. storing text input in the state.
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 */
@OrbitDsl
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.blockingIntent(
    registerIdling: Boolean = true,
    transformer: suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit
): Unit = runBlocking {
    container.inlineOrbit {
        withIdling(registerIdling) {
            Syntax(this).transformer()
        }
    }
}

/**
 * Build and execute a safe intent on [Container] with automatic fallback mechanism.
 *
 * This is a safer version of [intent] that provides fallback to direct execution
 * when the regular dispatch mechanism fails (e.g., after Compose Navigation).
 *
 * The method attempts to execute using regular [intent] first, and if that fails,
 * it falls back to executing using [Container.inlineOrbit] in an independent coroutine scope.
 *
 * @param registerIdling whether to register an idling resource when executing this intent. Defaults to true.
 * @param transformer lambda representing the transformer
 * @return [Job] representing the intent execution
 */
@OrbitDsl
@OrbitExperimental
public fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.safeIntent(
    registerIdling: Boolean = true,
    transformer: suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit
): Job {
    return try {
        container.intent(registerIdling) {
            Syntax(this).transformer()
        }
    } catch (e: Exception) {
        when (e) {
            is CancellationException -> throw e
            else -> createFallbackJob(transformer)
        }
    }
}
/**
 * Creates a fallback job that executes the transformer in a safe, independent coroutine scope.
 */
private fun <STATE : Any, SIDE_EFFECT : Any> ContainerHost<STATE, SIDE_EFFECT>.createFallbackJob(
    transformer: suspend Syntax<STATE, SIDE_EFFECT>.() -> Unit
): Job {
    val fallbackScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    return fallbackScope.launch {
        try {
            container.inlineOrbit {
                Syntax(this).transformer()
            }
        } catch (ex: Exception) {
            when (ex) {
                is TimeoutCancellationException -> throw ex
                is CancellationException -> throw ex
                else -> {
                    println("SafeIntent fallback execution failed: ${ex.message}")
                }
            }
        }
    }
}