# Orbit ViewModel plugin

The plugin provides [Container](../orbit-core/src/main/kotlin/org/orbitmvi/orbit/Container.kt)
  factory extensions on `ViewModel` for

- creating containers scoped with
  [ViewModelScope](https://developer.android.com/topic/libraries/architecture/coroutines)
  to automatically cancel the
  [Container](../orbit-core/src/main/kotlin/org/orbitmvi/orbit/Container.kt)
  whenever the `ViewModel` is cleared.
- saved state functionality via Jetpack's
  [Saved State module for ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)
  to automatically save and restore the
  [Container](../orbit-core/src/main/kotlin/org/orbitmvi/orbit/Container.kt)
  state on Activity or process death.

## Including the module

```kotlin
implementation("org.orbitmvi.orbit:orbit-viewmodel:<latest-version>")
```

## Creating a container in a ViewModel

This module contains a
[Container](../orbit-core/src/main/kotlin/org/orbitmvi/orbit/Container.kt)
factory extension function on `ViewModel` to facilitate creating a scoped
container.

``` kotlin
class ExampleViewModel : ContainerHost<ExampleState, Nothing>, ViewModel() {

    override val container = container<ExampleState, Nothing>(ExampleState())

    ...
}
```

## Saved state functionality

In order to automatically save state on process death or when your `Activity` is
destroyed there are two conditions:

1. Your State can be put into an Android
   [Bundle](https://developer.android.com/reference/android/os/Bundle). Most
   commonly this will mean you need to implement the
   [Parcelable](https://developer.android.com/reference/android/os/Parcelable)
   interface on your state object. Using Kotlin's
   [@Parcelize](https://kotlinlang.org/docs/reference/compiler-plugins.html#parcelable-implementations-generator)
   is recommended for ease of use.
1. You need to pass in a
   [SavedStateHandle](https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle)
   to your
   [Container](../orbit-core/src/main/kotlin/org/orbitmvi/orbit/Container.kt)
   factory function. The easiest way to do this is via
   [Koin's support](https://doc.insert-koin.io/#/koin-android/viewmodel?id=viewmodel-and-state-bundle).
   This can be set up using Dagger as well but this could mean creating your own
   custom equivalent of `androidx.lifecycle.SavedStateViewModelFactory`

Usage with Koin:

``` kotlin
// Declare the ViewModel with a saved state handle in your Koin module
val viewModelModule = module {
    viewModel { (handle: SavedStateHandle) -> MyViewModel(handle) }
}

// Inject as a stateViewModel in your Activity or Fragment
private val viewModel by stateViewModel<TodoViewModel>()

// Pass the SavedStateHandle to  your ViewModel
class ExampleViewModel(savedStateHandle: SavedStateHandle) : ContainerHost<ExampleState, Nothing>, ViewModel() {

    override val container = container<ExampleState, Nothing>(
        ExampleState(),
        savedStateHandle
    )

    ...
}
```
