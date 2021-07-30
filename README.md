# Orbit for Kotlin and Android

[![CI status](https://github.com/orbit-mvi/orbit-mvi/workflows/Android%20CI/badge.svg)](https://github.com/orbit-mvi/orbit-mvi/actions)
[![codecov](https://codecov.io/gh/orbit-mvi/orbit-mvi/branch/main/graph/badge.svg)](https://codecov.io/gh/orbit-mvi/orbit-mvi)
[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-core)](https://search.maven.org/artifact/org.orbit-mvi/orbit-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

![Logo](images/logo.png)

![slack logo](images/slack-logo-icon.png) [Join us at the Kotlinlang slack!](https://kotlinlang.slack.com/messages/CPM6UMD2P)

If you do not yet have an account with the Kotlinlang slack workspace,
[sign up here](https://slack.kotlinlang.org).

This library was originally developed at [Babylon Health](https://github.com/babylonhealth/orbit-mvi).
However, as its development over there ceased, further development
has been moved to this project maintained by the original authors.

## Documentation

- [How to write your own MVI library and why you shouldn't](https://www.youtube.com/watch?v=E6obYmkkdko)
- [Core module and architecture overview](orbit-core/README.md)
- [ViewModel](orbit-viewmodel/README.md)
- [Test](orbit-test/README.md)

## Overview

Orbit is a simple scaffolding you can build a Redux/MVI-like architecture
around.

- Easy to use, type-safe, extensible API
- Built on top of coroutines
- Compatible with [RxJava](orbit-core/docs/rxjava.md), [LiveData](orbit-core/docs/livedata.md)
  etc. through coroutine wrappers
- ViewModel support, along with SavedState!
- Unit test framework designed in step with the framework
- Built-in espresso idling resource support

And more!...

## Getting started in three simple steps

```kotlin
implementation("org.orbit-mvi:orbit-viewmodel:<latest-version>")
```

[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-viewmodel)](https://search.maven.org/artifact/org.orbit-mvi/orbit-viewmodel)

### Define the contract

First, we need to define its state and declared side effects.

``` kotlin
data class CalculatorState(
    val total: Int = 0
)

sealed class CalculatorSideEffect {
    data class Toast(val text: String) : CalculatorSideEffect()
}
```

The only requirement here is that the objects are comparable. We also recommend
they be immutable. Therefore we suggest using a mix of data classes, sealed
classes and objects.

### Create the ViewModel

Using the core Orbit functionality, we can create a simple, functional
ViewModel.

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
requirement to do so. You can host an Orbit
[Container](orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)
in a simple class if you wish. This makes it possible to use in simple Kotlin
projects as well as lifecycle independent services.

### Connect to the ViewModel in your Activity or Fragment

Now we need to wire up the `ViewModel` to our UI. We expose coroutine
`Flow`s through which one can conveniently subscribe to updates.
Alternatively you can convert these to your preferred type using
externally provided extension methods e.g.
[asLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#(kotlinx.coroutines.flow.Flow).asLiveData(kotlin.coroutines.CoroutineContext,%20kotlin.Long))
or
[asObservable](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-rx3/kotlinx.coroutines.rx3/kotlinx.coroutines.flow.-flow/as-observable.html).

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

## Syntax

``` kotlin
class MyViewModel: ContainerHost<MyState, MySideEffect>, ViewModel() {

    override val container = container<MyState, MySideEffect>(MyState())

    fun loadDataForId(id: Int) = intent {
        postSideEffect(MySideEffect.Toast("Loading data for $id!"))

        val result = repository.loadData(id)

        reduce {
            state.copy(data = result)
        }
    }
}
```

## Modules

Orbit is a modular framework. The Core module provides basic Orbit
functionality with additional features provided through optional modules.

Orbit supports using various async/stream frameworks at the same time so it is
perfect for legacy codebases. For example, it can support both [RxJava](orbit-core/docs/rxjava.md)
and coroutines if you are in the process of migrating from one to the other.

At the very least you will need the `orbit-core` module to get started,
alternatively include one of the other modules which already include
`orbit-core`.

```kotlin
implementation("org.orbit-mvi:orbit-core:<latest-version>")
implementation("org.orbit-mvi:orbit-viewmodel:<latest-version>")

testImplementation("org.orbit-mvi:orbit-test:<latest-version>")
```

[![Download](https://img.shields.io/maven-central/v/org.orbit-mvi/orbit-core)](https://search.maven.org/artifact/org.orbit-mvi/orbit-core)

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md)
for details on our code of conduct, and the process for submitting pull
requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions
available, see the [tags on this repository](https://github.com/orbit-mvi/orbit-mvi/tags).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

This project is licensed under the Apache License, Version 2.0 - see the
[LICENSE.md](LICENSE.md) file for details
