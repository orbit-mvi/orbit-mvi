---
sidebar_label: 'ViewModel (Multiplatform)'
---

# Android and Common ViewModel

The module provides [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
factory extensions on Android's [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) for:

- Creating containers scoped with
  [ViewModelScope](https://developer.android.com/topic/libraries/architecture/coroutines#viewmodelscope)
  to automatically cancel the
  [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
  whenever the `ViewModel` is cleared.
- Saved state functionality via Jetpack's
  [Saved State module for ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)
  to automatically save and restore the
  [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
  state on Activity or process death.

:::caution

Common ViewModel (Multiplatform) support added in Orbit v10.0.0.

:::

## Including the module

```kotlin
implementation("org.orbit-mvi:orbit-viewmodel:<latest-version>")
```

## Creating a container in a ViewModel

This module contains a
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
factory extension function on `ViewModel` to facilitate creating a scoped
container.

```kotlin
class ExampleViewModel : ContainerHost<ExampleState, Nothing>, ViewModel() {

    override val container = container<ExampleState, Nothing>(ExampleState())

    ...
}
```

## Saved state functionality with Kotlinx Serialization (Multiplatform)

To automatically save state on process death or UI destruction, two conditions
must be met:

1. For multiplatform, your state needs to be [serializable](https://github.com/Kotlin/kotlinx.serialization).
1. You must provide a 
   [SavedStateHandle](https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle)
   to the
   [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
   factory function, along with the
   [serializer](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/serializers.md)
   for your state.

```kotlin
@Serializable
data class ExampleState(
    ...
)

// Pass the SavedStateHandle and serializer to your ViewModel
class ExampleViewModel(savedStateHandle: SavedStateHandle) : ContainerHost<ExampleState, Nothing>, ViewModel() {

    override val container = container<ExampleState, Nothing>(
        initialState = ExampleState(),
        savedStateHandle = savedStateHandle,
        serializer = ExampleState.serializer()
    )

    ...
}
```

## Saved state functionality with Parcelable (Android only)

To automatically save state on process death or `Activity` destruction, two
conditions must be met:

1. Your state needs to be 
   [Parcelable](https://developer.android.com/reference/android/os/Parcelable).
   Using Kotlin's
   [@Parcelize](https://kotlinlang.org/docs/reference/compiler-plugins.html#parcelable-implementations-generator)
   is recommended for ease of use.
1. You must provide a
   [SavedStateHandle](https://developer.android.com/reference/androidx/lifecycle/SavedStateHandle)
   to the
   [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
   factory function.

```kotlin
@Parcelize
data class ExampleState(
    ...
) : Parcelable

// Pass the SavedStateHandle to your ViewModel
class ExampleViewModel(savedStateHandle: SavedStateHandle) : ContainerHost<ExampleState, Nothing>, ViewModel() {

    override val container = container<ExampleState, Nothing>(
        initialState = ExampleState(),
        savedStateHandle = savedStateHandle
    )

    ...
}
```

## SavedStateHandle limitations

On Android, `SavedStateHandle` stores data in a `Bundle`, which has key
limitations:

- **Not Reliable for Process Death:** It survives configuration changes but may
  lose data if the app is killed. Use 
  [Room](https://developer.android.com/training/data-storage/room) or
  [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
  for critical data.
- **Limited Data Size:** Bundles have size constraints, and large data can cause
  performance issues or 
  [TransactionTooLargeException](https://developer.android.com/reference/android/os/TransactionTooLargeException).
  Store only simple UI states.
- **Task Stack Dependency:** Data is lost if the task stack is cleared (e.g. app
  force-closed or removed from recents).

Best Practices:
- ✅ Use for small, UI-related state (e.g., IDs, enums).
- ❌ Avoid storing large or complex data—use persistent storage instead.
- 🔄 Understand it’s for transient UI state, not long-term persistence.
