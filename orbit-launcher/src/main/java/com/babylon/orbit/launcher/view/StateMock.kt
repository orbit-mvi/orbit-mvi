package com.babylon.orbit.launcher.view

class StateMock<STATE : Any> {

    private val _mocks: MutableMap<String, STATE?> by lazy {
        mutableMapOf(DEFAULT to defaultState)
    }

    val mocks: Map<String, STATE?> = _mocks

    val additionalMocks = mutableMapOf<String, STATE>()

    var defaultState: STATE? = null

    fun state(name: String, block: STATE.() -> STATE): StateMock<STATE> = apply {
        _mocks[name] = defaultState?.block()
    }

    companion object {

        const val DEFAULT = "Default"

        fun <STATE : Any> stateMocks(block: StateMock<STATE>.() -> StateMock<STATE>): StateMock<STATE> =
            block(StateMock())
    }
}
