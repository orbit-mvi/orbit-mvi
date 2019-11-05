package com.babylon.orbit.sample.presentation

import com.babylon.orbit.LifecycleAction
import com.babylon.orbit.OrbitViewModel

class TodoViewModel(
    private val transformers: TodoScreenTransformer,
    private val reducers: TodoScreenReducer,
    private val sideEffects: TodoScreenSideEffect
) : OrbitViewModel<TodoScreenState, Unit>(TodoScreenState(), {

    perform("load the todos")
        .on(
            LifecycleAction.Created::class.java,
            TodoScreenAction.RetryAction::class.java
        )
        .transform { transformers.loadTodos(eventObservable) }
        .withReducer { reducers.reduceLoadTodos(currentState, event) }

    perform("track analytics for selected todo")
        .on<TodoScreenAction.TodoSelected>()
        .sideEffect { sideEffects.trackSelectedTodo(event) }

    perform("load the selected todo")
        .on<TodoScreenAction.TodoSelected>()
        .withReducer { reducers.reduceLoadSelectedTodo(currentState, event) }

    perform("dismiss the selected todo")
        .on<TodoScreenAction.TodoSelectedDismissed>()
        .withReducer { reducers.reduceDismissSelectedTodo(currentState) }

    perform("load the user profile switch for the user profile")
        .on<TodoScreenAction.TodoUserSelected>()
        .transform { transformers.loadUserProfileSwitches(eventObservable) }
        .loopBack { event }

    perform("load the user profile is the switch is on")
        .on<UserProfileExtra>()
        .transform { transformers.loadUserProfile(eventObservable) }
        .withReducer { reducers.reduceLoadUserProfile(currentState, event) }

    perform("handle user profile switch is off")
        .on<UserProfileExtra>()
        .withReducer { reducers.reduceLoadUserProfileSwitch(currentState, event) }

    perform("dismiss the selected user")
        .on<TodoScreenAction.UserSelectedDismissed>()
        .withReducer { reducers.reduceUserSelected(currentState) }
})
