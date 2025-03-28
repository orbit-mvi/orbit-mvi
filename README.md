# Orbit Multiplatform

[![CI status](https://github.com/orbit-mvi/orbit-mvi/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/orbit-mvi/orbit-mvi/actions)
[![codecov](https://codecov.io/gh/orbit-mvi/orbit-mvi/branch/main/graph/badge.svg)](https://codecov.io/gh/orbit-mvi/orbit-mvi)
[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-core)](https://search.maven.org/artifact/org.orbit-mvi/orbit-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

![Logo](images/logo.png)

## Get in touch

[![slack logo](images/slack-logo-icon.png)](https://kotlinlang.slack.com/messages/CPM6UMD2P)
[![twitter logo](images/twitter-small.png)](https://twitter.com/orbit_mvi)

## What is Orbit

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

## Documentation

- [Getting Started](https://orbit-mvi.org)
- [Core](https://orbit-mvi.org/Core/)
- [Android and Common ViewModel](https://orbit-mvi.org/ViewModel/)
- [Jetpack Compose and Compose Multiplatform](https://orbit-mvi.org/Compose/)
- [Test](https://orbit-mvi.org/Test/)
- [Dokka source code documentation](https://orbit-mvi.org/dokka/)
- [Resources](https://orbit-mvi.org/resources)

### Articles & Talks

- [How to write your own MVI library and why you shouldn't](https://www.youtube.com/watch?v=E6obYmkkdko)
- [Top Android MVI libraries in 2021](https://appmattus.medium.com/de1afe890f27)
- [Orbit Multiplatform wins Kotlin Foundation Grant: A Journey and a Look Ahead](https://appmattus.medium.com/6b949cf8133e)

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

### Create the ViewModel

1. Implement the
   [ContainerHost](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)
   interface
1. Override the `container` field and use the `ViewModel.container` factory
   function to build an Orbit
   [Container](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)
   in your
   [ContainerHost](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)

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

We have used an Android `ViewModel` as the most common example, but there is no
requirement to do so.

### Connect to the ViewModel in your Activity or Fragment

```kotlin
class CalculatorActivity: AppCompatActivity() {

    // Example of injection using koin, your DI system might differ
    private val viewModel by viewModel<CalculatorViewModel>()

    override fun onCreate(savedState: Bundle?) {
        ...

        addButton.setOnClickListener { viewModel.add(1234) }

        viewModel.observe(state = ::render, sideEffect = ::handleSideEffect)
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

With Jetpack Compose wire up the ViewModel as follows:

```kotlin
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel) {

    val state = viewModel.collectAsState().value

    viewModel.collectSideEffect { handleSideEffect(it) }

    // render UI using data from 'state'
    ...
}

private fun handleSideEffect(sideEffect: CalculatorSideEffect) {
    when (sideEffect) {
        is CalculatorSideEffect.Toast -> toast(sideEffect.text)
    }
}
```

## Contributing

Please read [contributing](CONTRIBUTING.md)
for details on our code of conduct, and the process for submitting pull
requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions
available, see the
[tags on this repository](https://github.com/orbit-mvi/orbit-mvi/tags).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

This project is licensed under the Apache License, Version 2.0 - see the
[license](LICENSE.md) file for details
