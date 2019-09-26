package com.babylon.orbit.sample.presentation

import com.babylon.orbit.OrbitViewModel

class TodoActivityViewModel constructor(
    middleware: TodoMiddleware
) : OrbitViewModel<TodoScreenState, TodoScreenAction>(middleware)
