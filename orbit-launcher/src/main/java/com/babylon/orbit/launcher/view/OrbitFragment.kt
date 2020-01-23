package com.babylon.orbit.launcher.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.babylon.orbit.OrbitViewModel
import com.babylon.orbit.launcher.screenlist.ui.StatePickerDialogFragment
import com.babylon.orbit.launcher.serializer.SerializerFactory
import com.babylon.orbit.launcher.util.createFAB

@Suppress("TooManyFunctions")
abstract class OrbitFragment<STATE : Any, SIDE_EFFECT : Any> :
    Fragment(),
    OrbitView<STATE>,
    StatePickerDialogFragment.OnStateChanged {

    private val serializer by lazy {
        SerializerFactory<STATE>(requireContext().applicationContext).serializer
    }

    private val isRunningAsMockConsumer by lazy {
        arguments?.getBoolean(RUNNING_AS_MOCK_CONSUMER) ?: false
    }

    protected fun stateMocks(block: StateMock<STATE>.() -> StateMock<STATE>): StateMock<STATE> =
        block(StateMock())

    protected fun sendAction(action: Any) {
        if (!isRunningAsMockConsumer) {
            viewModel.sendAction(action)
        }
    }

    abstract val viewModel: OrbitViewModel<STATE, SIDE_EFFECT>

    abstract fun connect()

    open fun provideMocks(): StateMock<STATE> = StateMock()

    override val owner: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isRunningAsMockConsumer) {
            requireActivity()
                .window
                .decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .addView(
                    createFAB(
                        requireActivity() as AppCompatActivity,
                        this,
                        aggregatedStateMock()
                    )
                )
        }
    }

    override fun onStart() {
        super.onStart()

        if (isRunningAsMockConsumer) {
            provideMocks().defaultState?.let(this@OrbitFragment::render)
        } else {
            connect()
        }
    }

    @CallSuper
    override fun render(state: STATE) {
        if (!isRunningAsMockConsumer) serializer.serialize(this::class.java, state)
    }

    override fun onChange(stateName: String) {
        aggregatedStateMock()
            .let { it.mocks.plus(it.additionalMocks)[stateName] }
            ?.let(::render)
    }

    private fun aggregatedStateMock() =
        provideMocks()
            .apply {
                defaultState?.let { state ->
                    additionalMocks.putAll(
                        serializer.deserialize(
                            this@OrbitFragment::class.java,
                            state::class.java
                        ).toMap()
                    )
                }
            }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden && !isRunningAsMockConsumer) serializer.cleanup()
    }

    override fun onStop() {
        super.onStop()

        if (!isRunningAsMockConsumer) serializer.cleanup()
    }

    companion object {

        internal fun createAsMockConsumer(
            viewClass: Class<out OrbitView<*>>
        ) = (viewClass.newInstance() as Fragment).apply {
            arguments = Bundle().apply {
                putBoolean(RUNNING_AS_MOCK_CONSUMER, true)
            }
        }

        private const val RUNNING_AS_MOCK_CONSUMER =
            "com.babylon.orbit.launcher.OrbitActivity_RUNNING_AS_MOCK_CONSUMER"
    }
}
