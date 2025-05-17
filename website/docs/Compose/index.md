---
sidebar_label: 'Compose (Multiplatform)'
---

import CodeBlock from "@theme/CodeBlock";
import latestRelease from "@site/src/plugins/github-latest-release/generated/data.json";

# Jetpack Compose and Compose Multiplatform

The module provides [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
extensions for easy subscription from `Composables`.

:::caution

Compose Multiplatform support added in Orbit v10.0.0.

:::

## Including the module

<CodeBlock language="kotlin">implementation("org.orbit-mvi:orbit-compose:{latestRelease.tag_name}")</CodeBlock>

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

## TextField state hoisting

When dealing with `TextField` in Compose we often want the `ViewModel` to be the
owner of the state so we can handle validation and/or other logic such as
autocomplete suggestions.

It may seem natural to use `TextField.onChangeValue` to process the input
through an `intent`, with state emitting an updated value to
`TextField.value`, however, threading in `TextField` means user input is
partially lost.

Alternatively, `TextField` can be provided a `TextFieldState` which we can
provide and observer in our the `ViewModel`:

```kotlin
class TextViewModel : ViewModel(), ContainerHost<TextViewModel.State, Nothing> {
    override val container: Container<State, Nothing> = container(State()) {
        coroutineScope {
            launch {
                snapshotFlow { state.textFieldState.text }.collectLatest { text ->
                    reduce { state.copy(isValid = text.isValid()) }
                }
            }
        }
    }

    data class State(
        val textFieldState: TextFieldState = TextFieldState(""),
        val isValid: Boolean = false,
    )

    companion object {
        fun CharSequence.isValid(): Boolean {
            return this.isNotBlank() && this.length <= 10
        }
    }
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
