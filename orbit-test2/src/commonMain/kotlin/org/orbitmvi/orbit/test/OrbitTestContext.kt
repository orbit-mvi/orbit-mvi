package org.orbitmvi.orbit.test

public interface OrbitTestContext<STATE : Any, SIDE_EFFECT : Any> {

    public val previousState: STATE

    /**
     * Cancel this [OrbitTestContext] and ignore any events which have already been received.
     *
     * If it is backed by an underlying coroutine (e.g. the coroutine run
     * by [test]), that coroutine will also be cancelled. If called within a [test] block, the [test] block
     * will exit.
     */
    public suspend fun cancelAndIgnoreRemainingItems()

    /**
     * Sanity check assertion. Checks if the initial state is emitted and matches
     * the initial state defined for the production container or the one specified
     * in the test.
     *
     * @throws AssertionError if initial state does not match.
     */
    public suspend fun expectInitialState()

    /**
     * Return the next item received.
     * This function will suspend if no items have been received.
     */
    public suspend fun awaitItem(): Item<STATE, SIDE_EFFECT>

    /**
     * Return the next state received.
     * This function will suspend if no states have been received.
     *
     * @throws AssertionError if the most recent item was not a state.
     */
    public suspend fun awaitState(): STATE

    /**
     * Return the next side effect received.
     * This function will suspend if no side effects have been received.
     *
     * @throws AssertionError if the most recent item was not a side effect.
     */
    public suspend fun awaitSideEffect(): SIDE_EFFECT

    /**
     * Assert that [count] items were received and ignore them.
     * This function will suspend if no items have been received.
     */
    public suspend fun skipItems(count: Int)
}
