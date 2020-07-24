# Orbit 2 Saved State plugin

The plugin provides:

- [Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt)
  factory extensions for ViewModel saved state functionality via
  [Saved State module for ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)
  
This allows you to automatically save and restore the container state on process
death.

## Including the module

```kotlin
implementation("com.babylon.orbit2:orbit-savedstate:<latest-version>")
```

## Saved state functionality

In order to automatically save state on on process death there are two
conditions:

1. Your State can be put into an Android `Bundle`. Most commonly this will mean
   you need to implement the `Parcelable` interface on your state object.
   Using Kotlin's `@Parcelize`  is recommended for ease of use.
1. You need to pass in a `SavedStateHandle` to your container factory function.
   The easiest way to do this is via [Koin's support](https://doc.insert-koin.io/#/koin-android/viewmodel?id=viewmodel-and-state-bundle).
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
class ExampleViewModel(savedStateHandle: SavedStateHandle) : ContainerHost<ExampleState, Nothing> {
    override val container: Container<ExampleState, Nothing> = Container.createWithSavedState(
        ExampleState(),
        savedStateHandle
    )

    ...
}

```
