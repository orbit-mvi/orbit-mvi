package com.babylon.orbit.sample.di

import androidx.lifecycle.SavedStateHandle
import com.babylon.orbit.sample.domain.analytics.AnalyticsManager
import com.babylon.orbit.sample.domain.analytics.AnalyticsManagerImpl
import com.babylon.orbit.sample.domain.todo.GetTodoUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileSwitchesUseCase
import com.babylon.orbit.sample.domain.user.GetUserProfileUseCase
import com.babylon.orbit.sample.network.TodoService
import com.babylon.orbit.sample.network.TodoServiceImpl
import com.babylon.orbit.sample.network.UserService
import com.babylon.orbit.sample.network.UserServiceImpl
import com.babylon.orbit.sample.presentation.TodoScreenReducer
import com.babylon.orbit.sample.presentation.TodoScreenSideEffect
import com.babylon.orbit.sample.presentation.TodoScreenTransformer
import com.babylon.orbit.sample.presentation.TodoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val domainModule = module {

    single { GetTodoUseCase(get()) }

    single<TodoService> { TodoServiceImpl() }

    single<AnalyticsManager> { AnalyticsManagerImpl() }

    single<UserService> { UserServiceImpl() }

    single { GetUserProfileSwitchesUseCase(get()) }

    single { GetUserProfileUseCase(get()) }
}

val presentationModule = module {

    single { TodoScreenTransformer(get(), get(), get()) }

    single { TodoScreenReducer() }

    single { TodoScreenSideEffect(get()) }

    viewModel(useState = true) { (handle: SavedStateHandle) -> TodoViewModel(handle, get(), get(), get()) }
}
