package org.orbitmvi.orbit.test

public interface OrbitTestContext<STATE : Any, SIDE_EFFECT : Any> {

    public val previousState: STATE

    /**
     * Cancel this [ReceiveTurbine].
     *
     * If it is backed by an underlying coroutine (e.g. the coroutine run
     * by [test]), that coroutine will also be cancelled.
     *
     * If [cancel] is invoked before the underlying coroutine or channel has been closed, [ensureAllEventsConsumed]
     * will succeed even if the terminal event is not consumed.
     */
    public suspend fun cancel()

    /**
     * Cancel this [OrbitTestContext] and ignore any events which have already been received.
     *
     * If it is backed by an underlying coroutine (e.g. the coroutine run
     * by [test]), that coroutine will also be cancelled. If called within a [test] block, the [test] block
     * will exit.
     */
    public suspend fun cancelAndIgnoreRemainingEvents()

    /**
     * Assert that there are no unconsumed events which have already been received.
     *
     * @throws AssertionError if unconsumed events are found.
     */
    public suspend fun expectInitialState()

    /**
     * Returns the most recent item that has already been received.
     * If a complete event has been received with no item being received
     * previously, this function will throw an [AssertionError]. If an error event
     * has been received, this function will throw the underlying exception.
     *
     * @throws AssertionError if no item was emitted.
     */
    public fun expectMostRecentItem(): Item<STATE, SIDE_EFFECT>

    /**
     * Assert that the next event received was an item and return it.
     * This function will suspend if no events have been received.
     *
     * When this [OrbitTestContext] is in a terminal state ([Event.Complete] or [Event.Error], this method
     * will yield the same result every time it is called.
     *
     * @throws AssertionError if the next event was completion or an error.
     */
    public suspend fun awaitItem(): Item<STATE, SIDE_EFFECT>

    public suspend fun awaitState(): STATE

    public suspend fun awaitSideEffect(): SIDE_EFFECT

    /**
     * Assert that [count] item events were received and ignore them.
     * This function will suspend if no events have been received.
     *
     * @throws AssertionError if one of the events was completion or an error.
     */
    public suspend fun skipItems(count: Int)
}
