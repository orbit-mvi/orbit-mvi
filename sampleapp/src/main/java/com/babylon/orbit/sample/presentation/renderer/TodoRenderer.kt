package com.babylon.orbit.sample.presentation.renderer

import android.content.Context
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babylon.orbit.OrbitViewModel
import com.babylon.orbit.sample.R
import com.babylon.orbit.sample.domain.user.UserProfile
import com.babylon.orbit.sample.presentation.ScreenState
import com.babylon.orbit.sample.presentation.TodoScreenAction
import com.babylon.orbit.sample.presentation.TodoScreenState
import com.babylon.orbit.sample.presentation.ui.LogoItem
import com.babylon.orbit.sample.presentation.ui.SpaceItemDecoration
import com.babylon.orbit.sample.presentation.ui.ToDoItem
import com.babylon.orbit.sample.presentation.ui.show
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section

class TodoRenderer(
    private val context: Context,
    private val viewModel: OrbitViewModel<TodoScreenState, Unit>
) {

    private var todoDialog: AppCompatDialog? = null
    private var userProfileDialog: AppCompatDialog? = null
    private val section = Section()

    fun prepare(
        recyclerView: RecyclerView,
        retryButton: Button
    ) {
        section.setHeader(LogoItem())

        val space = context.resources.getDimension(R.dimen.container_padding).toInt()
        val decoration = SpaceItemDecoration(horizontalSpacing = space, verticalSpacing = space)
        recyclerView.addItemDecoration(decoration)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GroupAdapter<GroupieViewHolder>().apply {
                add(section)
            }
        }

        retryButton.setOnClickListener {
            viewModel.sendAction(TodoScreenAction.RetryAction)
        }
    }

    fun render(
        state: TodoScreenState,
        recyclerView: RecyclerView,
        progressContainer: View,
        errorContainer: View
    ) {
        progressContainer.show(state.screenState == ScreenState.Loading)
        errorContainer.show(state.screenState == ScreenState.Error)
        recyclerView.show(state.screenState == ScreenState.Ready)

        if (state.screenState == ScreenState.Ready) {
            if (state.todoList.isNullOrEmpty()) {
                section.clear()
            } else {
                state.todoList.map { todo ->
                    ToDoItem(
                        context.applicationContext.resources,
                        { viewModel.sendAction(it) },
                        todo
                    )
                }.let(section::update)
            }
            state.todoSelectedId?.let { showTodoDialog(it) }
            state.userProfile?.let { showUserProfileDialog(it) }
        }
    }

    private fun showTodoDialog(todoId: Int) {
        if (todoDialog == null) {
            todoDialog = AlertDialog.Builder(context)
                .setTitle(R.string.todo_dialog_title)
                .setMessage(context.getString(R.string.todo_dialog_msg, todoId))
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setOnDismissListener {
                    viewModel.sendAction(TodoScreenAction.TodoSelectedDismissed)
                    todoDialog = null
                }
                .create()
                .also { it.show() }
        }
    }

    private fun showUserProfileDialog(userProfile: UserProfile) {
        if (userProfileDialog == null) {
            userProfileDialog = AlertDialog.Builder(context)
                .setTitle(R.string.user_dialog_title)
                .setMessage(context.getString(R.string.user_dialog_msg, userProfile.name))
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setOnDismissListener {
                    viewModel.sendAction(TodoScreenAction.UserSelectedDismissed)
                    userProfileDialog = null
                }
                .create()
                .also { it.show() }
        }
    }
}
