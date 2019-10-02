package com.babylon.orbit.sample.presentation

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.babylon.orbit.sample.R
import com.babylon.orbit.sample.domain.user.UserProfile
import com.babylon.orbit.sample.presentation.ui.LogoItem
import com.babylon.orbit.sample.presentation.ui.SpaceItemDecoration
import com.babylon.orbit.sample.presentation.ui.ToDoItem
import com.babylon.orbit.sample.presentation.ui.show
import com.babylon.orbit.sample.presentation.ui.throttledClick
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TodoActivity : AppCompatActivity() {

    private val viewModel by viewModel<TodoActivityViewModel>()
    private val scopeProvider: AndroidLifecycleScopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    private val actionPublisher = PublishSubject.create<TodoScreenAction>()
    private var todoDialog: AppCompatDialog? = null
    private var userProfileDialog: AppCompatDialog? = null
    private val section = Section()

    private val actions by lazy {
        Observable.merge(
            listOf(
                actionPublisher,
                retryButton.throttledClick().map { TodoScreenAction.RetryAction }
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        section.setHeader(LogoItem())

        val space = resources.getDimension(R.dimen.container_padding).toInt()
        val decoration = SpaceItemDecoration(horizontalSpacing = space, verticalSpacing = space)
        recyclerView.addItemDecoration(decoration)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TodoActivity)
            adapter = GroupAdapter<GroupieViewHolder>().apply {
                add(section)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.connect(scopeProvider, actions, ::render)
    }

    private fun render(state: TodoScreenState) {
        progress_container.show(state.screenState == ScreenState.Loading)
        error_container.show(state.screenState == ScreenState.Error)
        recyclerView.show(state.screenState == ScreenState.Ready)

        if (state.screenState == ScreenState.Ready) {
            state.todoList?.map { todo -> ToDoItem(applicationContext.resources, actionPublisher, todo) }?.let {
                section.update(it)
            }
            state.todoSelectedId?.let { showTodoDialog(it) }
            state.userProfile?.let { showUserProfileDialog(it) }
        }
    }

    private fun showTodoDialog(todoId: Int) {
        if (todoDialog == null) {
            todoDialog = AlertDialog.Builder(this)
                .setTitle(R.string.todo_dialog_title)
                .setMessage(getString(R.string.todo_dialog_msg, todoId))
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setOnDismissListener {
                    actionPublisher.onNext(TodoScreenAction.TodoSelectedDismissed)
                    todoDialog = null
                }
                .create()
                .also { it.show() }
        }
    }

    private fun showUserProfileDialog(userProfile: UserProfile) {
        if (userProfileDialog == null) {
            userProfileDialog = AlertDialog.Builder(this)
                .setTitle(R.string.user_dialog_title)
                .setMessage(getString(R.string.user_dialog_msg, userProfile.name))
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setOnDismissListener {
                    actionPublisher.onNext(TodoScreenAction.UserSelectedDismissed)
                    todoDialog = null
                }
                .create()
                .also { it.show() }
        }
    }
}
