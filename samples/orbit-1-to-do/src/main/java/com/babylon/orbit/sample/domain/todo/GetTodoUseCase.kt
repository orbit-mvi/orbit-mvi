package com.babylon.orbit.sample.domain.todo

import com.babylon.orbit.sample.network.TodoService
import io.reactivex.Observable

class GetTodoUseCase(
    private val todoService: TodoService
) {

    fun getTodoList(): Observable<TodoStatus> {
        return todoService.getTodo()
            .map<TodoStatus> { TodoStatus.Result(it) }
            .toObservable()
            .startWith(TodoStatus.Loading)
            .onErrorReturn { TodoStatus.Failure(it) }
    }
}
