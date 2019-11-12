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
        .reduce { reducers.reduceLoadTodos(getCurrentState(), event) }

    perform("track analytics for selected todo")
        .on<TodoScreenAction.TodoSelected>()
        .sideEffect { sideEffects.trackSelectedTodo(event) }

    perform("load the selected todo")
        .on<TodoScreenAction.TodoSelected>()
        .reduce { reducers.reduceLoadSelectedTodo(getCurrentState(), event) }

    perform("dismiss the selected todo")
        .on<TodoScreenAction.TodoSelectedDismissed>()
        .reduce { reducers.reduceDismissSelectedTodo(getCurrentState()) }

    perform("load the user profile switch for the user profile")
        .on<TodoScreenAction.TodoUserSelected>()
        .transform { transformers.loadUserProfileSwitches(eventObservable) }
        .loopBack { event }

    perform("load the user profile is the switch is on")
        .on<UserProfileExtra>()
        .transform { transformers.loadUserProfile(eventObservable) }
        .reduce { reducers.reduceLoadUserProfile(getCurrentState(), event) }

    perform("handle user profile switch is off")
        .on<UserProfileExtra>()
        .reduce { reducers.reduceLoadUserProfileSwitch(getCurrentState(), event) }

    perform("dismiss the selected user")
        .on<TodoScreenAction.UserSelectedDismissed>()
        .reduce { reducers.reduceUserSelected(getCurrentState()) }
})
