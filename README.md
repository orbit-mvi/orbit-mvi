# Orbit Multiplatform

[![CI status](https://github.com/orbit-mvi/orbit-mvi/workflows/Android%20CI/badge.svg)](https://github.com/orbit-mvi/orbit-mvi/actions)
[![codecov](https://codecov.io/gh/orbit-mvi/orbit-mvi/branch/main/graph/badge.svg)](https://codecov.io/gh/orbit-mvi/orbit-mvi)
[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-core)](https://search.maven.org/artifact/org.orbit-mvi/orbit-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

![Logo](images/logo.png)

## Get in touch

[![slack logo](images/slack-logo-icon.png)](https://kotlinlang.slack.com/messages/CPM6UMD2P)
[![twitter logo](images/twitter-small.png)](https://twitter.com/orbit_mvi)

## What is Orbit

Orbit is a Redux/MVI-like library - but without the baggage. It's so simple we
think of it as MVVM+.

- Simple, type-safe, coroutine-style, extensible API
- Multiplatform, targetting Android and iOS (iOS support is in alpha and being
  actively worked on)
- Full support for Kotlin Coroutines (it's built on top of them after all)
- Lifecycle-safe collection of infinite flows
- ViewModel support, along with SavedState
- Optional, simple unit test library
- Built-in espresso idling resource support
- Compatible with [RxJava](https://orbit-mvi.org/Core/rxjava), [LiveData](https://orbit-mvi.org/Core/livedata.md)
  etc. through coroutine wrappers
- And more...

## Documentation

- [Getting Started](https://orbit-mvi.org)
- [Core](https://orbit-mvi.org/Core/overview)
- [Android ViewModel](https://orbit-mvi.org/Android-Viewmodel/overview)
- [Test](https://orbit-mvi.org/Test/overview)
- [Dokka source code documentation](https://orbit-mvi.org/dokka/)
- [Resources](https://orbit-mvi.org/resources)

### Articles & Talks

- [How to write your own MVI library and why you shouldn't](https://www.youtube.com/watch?v=E6obYmkkdko)
- [Top Android MVI libraries in 2021](https://appmattus.medium.com/top-android-mvi-libraries-in-2021-de1afe890f27)

## Getting started

[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-viewmodel)](https://search.maven.org/artifact/org.orbit-mvi/orbit-viewmodel)

```kotlin
implementation("org.orbit-mvi:orbit-core:<latest-version>")
// or, if on Android:
implementation("org.orbit-mvi:orbit-viewmodel:<latest-version>")

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

### Create the ViewModel

1. Implement the
   [ContainerHost](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)
   interface
1. Override the `container` field and use the `ViewModel.container` factory
   function to build an Orbit
   [Container](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)
   in your
   [ContainerHost](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)

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

We have used an Android `ViewModel` as the most common example, but there is no
requirement to do so.

### Connect to the ViewModel in your Activity or Fragment

``` kotlin
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

    val state = viewModel.container.stateFlow.collectAsState().value

    LaunchedEffect(viewModel) {
        launch {
            viewModel.container.sideEffectFlow.collect { handleSideEffect(navController, it) }
        }
    }

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
available, see the [tags on this repository](https://github.com/orbit-mvi/orbit-mvi/tags).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

This project is licensed under the Apache License, Version 2.0 - see the
[license](LICENSE.md) file for details
