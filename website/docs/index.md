---
sidebar_label: 'Getting started'
---

# Orbit Multiplatform

![Logo](images/logo.png)

Orbit is a simple, type-safe MVI framework for Kotlin Multiplatform, enabling
shared business logic across Android, iOS and desktop. With a Redux/MVI-inspired
unidirectional data flow, it streamlines state management within MVVM—think of
it as MVVM+.

Key features:
- **Multiplatform Support:** Share code seamlessly across Android, iOS and
  desktop.
- **Lifecycle-Safe Flows:** Collect infinite flows safely, preventing memory
  leaks.
- **Multiplatform ViewModel & SavedState:** Manage UI state efficiently across
  platforms including being able to save state.
- **Compose Multiplatform:** Build declarative UIs with shared code.
- **Testing and Tooling:** Includes unit tests and Espresso idling resource
  support.

## Getting started

[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-viewmodel)](https://search.maven.org/artifact/org.orbit-mvi/orbit-viewmodel)

```kotlin
// Core of Orbit, providing state management and unidirectional data flow (multiplatform)
implementation("org.orbit-mvi:orbit-core:<latest-version>")

// Integrates Orbit with Android and Common ViewModel for lifecycle-aware state handling (Android, iOS, desktop)
implementation("org.orbit-mvi:orbit-viewmodel:<latest-version>")

// Enables Orbit support for Jetpack Compose and Compose Multiplatform (Android, iOS, desktop)
implementation("org.orbit-mvi:orbit-compose:<latest-version>")

// Simplifies testing with utilities for verifying state and event flows (multiplatform)
testImplementation("org.orbit-mvi:orbit-test:<latest-version>")
```

### Define the contract

```kotlin
data class CalculatorState(
    val total: Int = 0
)

sealed class CalculatorSideEffect {
    data class Toast(val text: String) : CalculatorSideEffect()
}
```

The state and side effect objects must be comparable and we also recommend
they be immutable. We suggest using a mix of data classes, sealed classes and
objects.

### Create the ViewModel

1. Implement the
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   interface
1. Override the `container` field and use the `ViewModel.container` factory
   function to build an Orbit
   [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
   in your
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)

```kotlin
class CalculatorViewModel: ContainerHost<CalculatorState, CalculatorSideEffect>, ViewModel() {

    // Include `orbit-viewmodel` for the factory function
    override val container = container<CalculatorState, CalculatorSideEffect>(CalculatorState())

    fun add(number: Int) = intent {
        postSideEffect(CalculatorSideEffect.Toast("Adding $number to ${state.total}!"))

        reduce {
            state.copy(total = state.total + number)
        }
    }
}
```

We have used an Android `ViewModel` as the most common example, but it's not
required. You can host an Orbit
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
in a simple Kotlin class if you wish. This makes it possible to use in UI
independent components as well as Kotlin Multiplatform projects.

### Connect to the ViewModel in your Activity or Fragment

On Android, we expose an easy one-liner function to connect your UI to the
ViewModel. Alternatively, you can use the
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)'s
`Flow`s directly.

```kotlin
class CalculatorActivity: AppCompatActivity() {

    // Example of injection using koin, your DI system might differ
    private val viewModel by viewModel<CalculatorViewModel>()

    override fun onCreate(savedState: Bundle?) {
        ...
        addButton.setOnClickListener { viewModel.add(1234) }

        // Use the one-liner from the orbit-viewmodel module to observe when
        // Lifecycle.State.STARTED
        viewModel.observe(state = ::render, sideEffect = ::handleSideEffect)

        // Or observe the streams directly
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.container.refCountStateFlow.collect { render(it) }
                }
                launch {
                    viewModel.container.refCountSideEffectFlow.collect { handleSideEffect(it) }
                }
            }
        }
    }

    private fun render(state: CalculatorState) {
        ...
    }

    private fun handleSideEffect(sideEffect: CalculatorSideEffect) {
        when (sideEffect) {
            is CalculatorSideEffect.Toast -> toast(sideEffect.text)
        }
    }
}
```
