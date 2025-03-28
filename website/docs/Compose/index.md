---
sidebar_label: 'Compose (Multiplatform)'
---

# Jetpack Compose and Compose Multiplatform

The module provides [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
extensions for easy subscription from `Composables`.

:::caution

Compose Multiplatform support added in Orbit v10.0.0.

:::

## Including the module

```kotlin
implementation("org.orbit-mvi:orbit-compose:<latest-version>")
```

## Subscribing to a ContainerHost in Compose

Use the method below to subscribe to a [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
in Compose.

The functions safely follow the Composable lifecycle and will automatically
subscribe only if the view is at least `STARTED`.

```kotlin
@Composable
fun SomeScreen(viewModel: SomeViewModel) {
    val state by viewModel.collectAsState()
  
    viewModel.collectSideEffect {
        when(it) {
            ...
        }
    }

    SomeContent(
        state = state
    )
}
```

## Compose UI Testing

For better testability, separate the `ViewModel` from your UI. As shown above,
access the `ViewModel` only in `SomeScreen`, passing its state to `SomeContent`
for rendering. This makes it easy to test `SomeContent` in isolation, without
concerns about state conflation from the `ViewModel`.

Similarly, `ViewModel` callbacks can be passed into `SomeContent`, either
directly, or through an interface.

```kotlin
SomeContent(
    state = state,
    onSubmit = { viewModel.submit() }
)

// or

interface SomeCallbacks {
    fun onSubmit()
}

SomeContent(
    state = state,
    callbacks = object : SomeCallbacks {
        override fun onSubmit() = viewModel.submit()
    }
)

```
