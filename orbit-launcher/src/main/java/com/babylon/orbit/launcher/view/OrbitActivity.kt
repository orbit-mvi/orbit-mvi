package com.babylon.orbit.launcher.view

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.babylon.orbit.OrbitViewModel
import com.babylon.orbit.launcher.screenlist.ui.StatePickerDialogFragment
import com.babylon.orbit.launcher.serializer.SerializerFactory
import com.babylon.orbit.launcher.util.createFAB

abstract class OrbitActivity<STATE : Any, SIDE_EFFECT : Any> :
    AppCompatActivity(),
    OrbitView<STATE>,
    StatePickerDialogFragment.OnStateChanged {

    private val serializer by lazy {
        SerializerFactory<STATE>(applicationContext).serializer
    }

    private val isRunningAsMockConsumer by lazy {
        intent.getBooleanExtra(RUNNING_AS_MOCK_CONSUMER, false)
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

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)

        if (isRunningAsMockConsumer) {
            window
                .decorView
                .findViewById<ViewGroup>(android.R.id.content)
                .addView(
                    createFAB(
                        this,
                        this,
                        aggregatedStateMock()
                    )
                )
        }
    }

    override fun onStart() {
        super.onStart()

        if (isRunningAsMockConsumer) {
            provideMocks().defaultState?.let(this@OrbitActivity::render)
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
                            this@OrbitActivity::class.java,
                            state::class.java
                        ).toMap()
                    )
                }
            }

    override fun onStop() {
        super.onStop()

        if (!isRunningAsMockConsumer) serializer.cleanup()
    }

    companion object {

        internal fun startAsMockConsumer(
            context: Context,
            viewClass: Class<out OrbitView<*>>
        ) = context.startActivity(
            Intent(context, viewClass)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
                .putExtra(RUNNING_AS_MOCK_CONSUMER, true)
        )

        private const val RUNNING_AS_MOCK_CONSUMER =
            "com.babylon.orbit.launcher.OrbitActivity_RUNNING_AS_MOCK_CONSUMER"
    }
}
