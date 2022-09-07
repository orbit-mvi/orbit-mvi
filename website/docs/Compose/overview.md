---
sidebar_position: 1
sidebar_label: 'Overview'
---

# Jetpack Compose module

The module provides [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
extensions for easy subscription from `Composables`.

## Including the module

```kotlin
implementation("org.orbit-mvi:orbit-compose:<latest-version>")
```

## Subscribing to a ContainerHost in Compose

Use the method below to subscribe to a [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
in Compose.

The functions safely follow the Composable lifecycle and will automatically
subscribe only if the view is at least `STARTED`.

``` kotlin
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

