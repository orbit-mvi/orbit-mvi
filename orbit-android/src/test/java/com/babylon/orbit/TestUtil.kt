package com.babylon.orbit

fun createTestMiddleware(
    initialState: TestState = TestState(42),
    block: OrbitsBuilder<TestState, String>.() -> Unit
) = middleware<TestState, String>(initialState) {
    this.apply(block)
}
