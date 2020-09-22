# Orbit 2 for Kotlin and Android

[![CI status](https://github.com/babylonhealth/orbit-mvi/workflows/Android%20CI/badge.svg)](https://github.com/babylonhealth/orbit-mvi/actions)
[![codecov](https://codecov.io/gh/babylonhealth/orbit-mvi/branch/main/graph/badge.svg)](https://codecov.io/gh/babylonhealth/orbit-mvi)
[![Download](https://api.bintray.com/packages/babylonpartners/maven/orbit-core/images/download.svg)](https://bintray.com/babylonpartners/maven/orbit-core/_latestVersion)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

![Logo](images/logo.png)

![slack logo](images/slack-logo-icon.png) [Join us at the Kotlinlang slack!](https://kotlinlang.slack.com/messages/CPM6UMD2P)

If you do not yet have an account with the Kotlinlang slack workspace,
[sign up here](https://slack.kotlinlang.org).

If you're looking for the original Orbit library,
[it's available here.](https://github.com/babylonhealth/orbit-mvi/tree/orbit/main)

## Overview

Orbit 2 is a simple scaffolding you can build a Redux/MVI-like architecture
around.

In Orbit 2 we have taken the best features of Orbit 1 and rewritten the rest
from scratch.

### Powerful and flexible design 🏋️‍♀️ 🤸‍♂️

- Integrates best practices from our 2+ years of experience with MVI
- Powered by coroutines
- Easy to use, type-safe, extensible API

### Works with any async/stream framework 🔀

- Coroutine, RxJava (1 2 & 3!) and LiveData operator support

### Orbit ❤️ Android

- Subscribe to state and side effects through LiveData
- ViewModel support, along with SavedState!

### Testing 🤖

- Unit test framework designed in step with the framework
- Built-in espresso idling resource support

And more!...

## Getting started

Orbit 2 is a modular framework. The Core module provides basic Orbit
functionality with additional features provided through optional modules.

Orbit supports using various async/stream frameworks at the same time so it is
perfect for legacy codebases. For example, it can support both RxJava 2 and
coroutines if you are in the process of migrating from one to the other.

At the very least you will need the `orbit-core` module to get started,
alternatively include one of the other modules which already include
`orbit-core`.

```kotlin
implementation("com.babylon.orbit2:orbit-core:<latest-version>")
implementation("com.babylon.orbit2:orbit-coroutines:<latest-version>")
implementation("com.babylon.orbit2:orbit-rxjava1:<latest-version>")
implementation("com.babylon.orbit2:orbit-rxjava2:<latest-version>")
implementation("com.babylon.orbit2:orbit-rxjava3:<latest-version>")
implementation("com.babylon.orbit2:orbit-livedata:<latest-version>")
implementation("com.babylon.orbit2:orbit-viewmodel:<latest-version>")

testImplementation("com.babylon.orbit2:orbit-test:<latest-version>")
```

[![Download](https://api.bintray.com/packages/babylonpartners/maven/orbit-core/images/download.svg)](https://bintray.com/babylonpartners/maven/orbit-core/_latestVersion)

For detailed documentation, see:

- [Core module and architecture overview](orbit-2-core/README.md)
- [Coroutines](orbit-2-coroutines/README.md)
- [RxJava 1](orbit-2-rxjava1/README.md)
- [RxJava 2](orbit-2-rxjava2/README.md)
- [RxJava 3](orbit-2-rxjava3/README.md)
- [LiveData](orbit-2-livedata/README.md)
- [ViewModel](orbit-2-viewmodel/README.md)
- [Test](orbit-2-test/README.md)

## Creating a simple Orbit 2 ViewModel

Using the core Orbit functionality, we can create a simple, functional
ViewModel.

### Include the dependencies

```kotlin
implementation("com.babylon.orbit2:orbit-viewmodel:<latest-version>")
```

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

Next, we can define the ViewModel.

1. Implement the
   [ContainerHost](orbit-2-core/src/main/java/com/babylon/orbit2/ContainerHost.kt)
   interface
1. Override the `container` field and use the `ViewModel.container` factory
   function to build an Orbit
   [Container](orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt) in
   your
   [ContainerHost](orbit-2-core/src/main/java/com/babylon/orbit2/ContainerHost.kt)

``` kotlin
class CalculatorViewModel: ContainerHost<CalculatorState, CalculatorSideEffect>, ViewModel() {

    // Include `orbit-viewmodel` for the factory function
    override val container = container<CalculatorState, CalculatorSideEffect>(CalculatorState())

    fun add(number: Int) = orbit {
        sideEffect {
            post(CalculatorSideEffect.Toast("Adding $number to ${state.total}!"))
        }
            .reduce {
                state.copy(total = state.total + number)
            }
    }

    fun subtract(number: Int) = orbit {
        sideEffect {
            post(CalculatorSideEffect.Toast("Subtracting $number from ${state.total}!"))
        }
            .reduce {
                state.copy(total = state.total - number)
            }
    }
}
```

We have used an Android `ViewModel` as the most common example, but there is no
requirement to do so. You can host an Orbit
[Container](orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt) in a
simple class if you wish. This makes it possible to use in simple Kotlin
projects as well as lifecycle independent services.

## Connecting to a ViewModel

Now we need to wire up the `ViewModel` to our UI. Orbit provides various methods
of connecting via optional modules. For Android, the most convenient way to
connect is via `LiveData`, as it manages subscription disposal automatically.

``` kotlin
class CalculatorActivity: AppCompatActivity() {

    // Example of injection using koin, your DI system might differ
    private val viewModel by viewModel<CalculatorViewModel>()

    override fun onCreate(savedState: Bundle?) {
        ...
        addButton.setOnClickListener { viewModel.add(1234) }
        subtractButton.setOnClickListener { viewModel.subtract(1234) }

        // NOTE: Live data support is provided by the live data module:
        // com.babylon.orbit2:orbit-livedata
        viewModel.container.stateLiveData.observe(this) { render(it) }
        viewModel.container.sideEffectLiveData.observe(this) { handleSideEffect(it) }
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

## A bit of history

We originally set out to create Orbit with the following principles in mind:

- Simple
- Flexible
- Testable
- Designed for, but not limited to Android

Orbit 1 was our first attempt at this, and while it worked well in general, it
fell short of our expectations when it came to its flexibility and testability.
It did not support coroutines with support hard to incorporate, as it was
rigidly dependent on RxJava 2. The users were not shielded from this either. As
we were migrating to coroutines ourselves, this was increasing the complexity
of our code.

We thought we had taken Orbit 1 as far as we could. Having learned a great deal
about MVI in Orbit 1, we set out to take another shot at this. We resolved to
keep the good things of Orbit 1 and redesign it from the ground up to live up
to our standards as Orbit 2. We think - hopefully, finally - we hit the sweet
spot.

We stand on the shoulders of giants:

- [Managing State with RxJava by Jake Wharton](https://www.reddit.com/r/androiddev/comments/656ter/managing_state_with_rxjava_by_jake_wharton/)
- [RxFeedback](https://github.com/NoTests/RxFeedback.kt)
- [Mosby MVI](https://github.com/sockeqwe/mosby)
- [MvRx](https://github.com/airbnb/MvRx)

Thank you so much to everyone in the community for the support, whether direct
or not.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md)
for details on our code of conduct, and the process for submitting pull
requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions
available, see the [tags on this repository](https://github.com/babylonhealth/orbit-mvi/tags).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

This project is licensed under the Apache License, Version 2.0 - see the
[LICENSE.md](LICENSE.md) file for details
