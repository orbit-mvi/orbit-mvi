---
sidebar_position: 1
sidebar_label: 'Getting started'
slug: /
---


# Orbit Multiplatform

![Logo](images/logo.png)

Orbit is a Redux/MVI-like library - but without the baggage. It's so simple we
think of it as MVVM+.

## Documentation

- [Core](Core/overview.md)
- [Android ViewModel](Android-ViewModel/overview.md)
- [Compose](Compose/overview.md)
- [Test](Test/new.md)
- [Dokka source code documentation](pathname://dokka/)
- [Resources](resources.md)

## Getting started

[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-viewmodel)](https://search.maven.org/artifact/org.orbit-mvi/orbit-viewmodel)

```kotlin
implementation("org.orbit-mvi:orbit-core:<latest-version>")
// or, if on Android:
implementation("org.orbit-mvi:orbit-viewmodel:<latest-version>")
// If using Jetpack Compose include
implementation("org.orbit-mvi:orbit-compose:<latest-version>")

// Tests
testImplementation("org.orbit-mvi:orbit-test:<latest-version>")
```

### Define the contract

``` kotlin
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
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
   interface
1. Override the `container` field and use the `ViewModel.container` factory
   function to build an Orbit
   [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
   in your
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)

``` kotlin
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

``` kotlin
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
                    viewModel.container.stateFlow.collect { render(it) }
                }
                launch {
                    viewModel.container.sideEffectFlow.collect { handleSideEffect(it) }
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
