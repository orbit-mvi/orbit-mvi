package com.babylon.orbit.sample.presentation

import com.babylon.orbit.LifecycleAction
import com.babylon.orbit.Middleware
import com.babylon.orbit.middleware

class TodoMiddleware(
    private val transformers: TodoScreenTransformer,
    private val reducers: TodoScreenReducer
) : Middleware<TodoScreenState, TodoScreenAction> by middleware(TodoScreenState(), {

    perform("load the todos")
        .on(
            LifecycleAction.Created::class.java,
            TodoScreenAction.RetryAction::class.java
        )
        .transform(transformers::loadTodos)
        .withReducer(reducers::reduceLoadTodos)

    perform("track analytics for selected todo")
        .on<TodoScreenAction.TodoSelected>()
        .transform(transformers::trackSelectedTodo)
        .ignoringEvents()

    perform("load the selected todo")
        .on<TodoScreenAction.TodoSelected>()
        .withReducer(reducers::reduceLoadSelectedTodo)

    perform("dismiss the selected todo")
        .on<TodoScreenAction.TodoSelectedDismissed>()
        .withReducer(reducers::reduceDismissSelectedTodo)

    perform("load the user profile switch for the user profile")
        .on<TodoScreenAction.TodoUserSelected>()
        .transform(transformers::loadUserProfileSwitches)
        .loopBack { it }

    perform("load the user profile is the switch is on")
        .on<UserProfileExtra>()
        .transform(transformers::loadUserProfile)
        .withReducer(reducers::reduceLoadUserProfile)

    perform("handle user profile switch is off")
        .on<UserProfileExtra>()
        .withReducer(reducers::reduceLoadUserProfileSwitch)

    perform("dismiss the selected user")
        .on<TodoScreenAction.UserSelectedDismissed>()
        .withReducer(reducers::reduceUserSelected)
})
