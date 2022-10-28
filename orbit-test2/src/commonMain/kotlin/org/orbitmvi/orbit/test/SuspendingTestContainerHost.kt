package org.orbitmvi.orbit.test

import app.cash.turbine.ReceiveTurbine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.orbitmvi.orbit.ContainerHost
import kotlin.coroutines.EmptyCoroutineContext

public class SuspendingTestContainerHost<STATE : Any, SIDE_EFFECT : Any, CONTAINER_HOST : ContainerHost<STATE, SIDE_EFFECT>>(
    private val actual: CONTAINER_HOST,
    initialState: STATE?,
    private val isolateFlow: Boolean,
    emissions: ReceiveTurbine<Item<STATE, SIDE_EFFECT>>
) : BaseTestContainerHost<STATE, SIDE_EFFECT, CONTAINER_HOST>(
    actual, initialState, emissions
) {
    /**
     * Invoke `onCreate` property for the [ContainerHost] backed by [LazyCreateContainerDecorator],
     * e.g.: created by [CoroutineScope.container]
     */
    public suspend fun runOnCreate() {
        super.runOnCreateInternal()
        actual.suspendingIntent(shouldIsolateFlow = false)
    }

    /**
     * Invoke an intent on the [ContainerHost] under test as a suspending function.
     */
    public suspend fun invokeIntent(action: CONTAINER_HOST.() -> Unit) {
        super.invokeIntentInternal(action)
        actual.suspendingIntent(isolateFlow)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun CONTAINER_HOST.suspendingIntent(shouldIsolateFlow: Boolean) {
        val testContainer = container.findTestContainer()

        var firstIntentExecuted = false
        while (!testContainer.savedIntents.isEmpty) {
            val intent = testContainer.savedIntents.receive()
            if (!shouldIsolateFlow || !firstIntentExecuted) {
                firstIntentExecuted = true
                val context = actual.container.settings.exceptionHandler?.plus(SupervisorJob()) ?: EmptyCoroutineContext
                coroutineScope {
                    launch(context) {
                        intent()
                    }
                }.join()
            }
        }
    }
}
