package com.babylon.orbit.sample.presentation

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.babylon.orbit.sample.R
import com.babylon.orbit.sample.domain.user.UserProfile
import com.babylon.orbit.sample.presentation.ui.TodoAdapter
import com.babylon.orbit.sample.presentation.ui.show
import com.babylon.orbit.sample.presentation.ui.throttledClick
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class TodoActivity : AppCompatActivity() {

    private val viewModel by viewModel<TodoActivityViewModel>()
    private val scopeProvider: AndroidLifecycleScopeProvider by lazy {
        AndroidLifecycleScopeProvider.from(
            this
        )
    }

    private val actionPublisher = PublishSubject.create<TodoScreenAction>()
    private var todoDialog: AppCompatDialog? = null
    private var userProfileDialog: AppCompatDialog? = null

    private val actions by lazy {
        Observable.merge(
            listOf(
                actionPublisher,
                retryButton.throttledClick().map { TodoScreenAction.RetryAction }
            )
        )
    }

    private val todoAdapter = TodoAdapter({
        actionPublisher.onNext(TodoScreenAction.TodoSelected(it))
    }, {
        actionPublisher.onNext(TodoScreenAction.TodoUserSelected(it))
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TodoActivity)
            adapter = todoAdapter
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
            state.todoList?.let { todoAdapter.todoItems = it }
            state.todoSelectedId?.let { showTodoDialog(it) }
            state.userProfile?.let { showUserProfileDialog(it) }
        }
    }

    private fun showTodoDialog(todoId: Int) {
        if (todoDialog == null) {
            todoDialog = AlertDialog.Builder(this)
                .setTitle(R.string.todo_dialog_title)
                .setMessage(getString(R.string.todo_dialog_msg, todoId))
                .setPositiveButton(R.string.all_ok, { _, _ -> })
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
                .setPositiveButton(R.string.all_ok, { _, _ -> })
                .setOnDismissListener {
                    actionPublisher.onNext(TodoScreenAction.UserSelectedDismissed)
                    todoDialog = null
                }
                .create()
                .also { it.show() }
        }
    }
}
