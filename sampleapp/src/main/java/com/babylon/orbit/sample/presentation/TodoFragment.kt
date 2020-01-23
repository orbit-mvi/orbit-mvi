package com.babylon.orbit.sample.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.babylon.orbit.launcher.view.OrbitFragment
import com.babylon.orbit.sample.R
import com.babylon.orbit.sample.presentation.mock.TodoScreenStateMocks
import com.babylon.orbit.sample.presentation.renderer.TodoRenderer
import kotlinx.android.synthetic.main.todo_view.*
import org.koin.androidx.viewmodel.ext.android.stateViewModel

class TodoFragment : OrbitFragment<TodoScreenState, Unit>() {

    private val renderer by lazy { TodoRenderer(requireContext(), viewModel) }

    override val viewModel by stateViewModel<TodoViewModel>()

    override fun connect() = viewModel.connect(this, ::render)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.todo_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderer.prepare(
            recyclerView = recycler_view,
            retryButton = retry_button
        )
    }

    override fun render(state: TodoScreenState) {
        super.render(state)

        renderer.render(
            state = state,
            recyclerView = recycler_view,
            progressContainer = progress_container,
            errorContainer = error_container
        )
    }

    override fun provideMocks() = TodoScreenStateMocks.mocks
}
