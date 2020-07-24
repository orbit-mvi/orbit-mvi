package com.babylon.orbit.sample.network

import com.babylon.orbit.sample.domain.todo.Todo
import io.reactivex.Single

interface TodoService {

    fun getTodo(): Single<List<Todo>>
}
