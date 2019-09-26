package com.babylon.orbit.sample.network

import com.babylon.orbit.sample.domain.todo.Todo
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class TodoServiceImpl : TodoService {

    @SuppressWarnings("MagicNumber")
    override fun getTodo(): Single<List<Todo>> {
        return Single.just(listOf(
            Todo(1, 1, "first todo"),
            Todo(2, 2, "second todo"),
            Todo(3, 3, "third todo"),
            Todo(4, 4, "fourth todo"),
            Todo(5, 5, "fifth todo"),
            Todo(6, 6, "sixth todo")
        )).delay(2, TimeUnit.SECONDS)
    }
}
