# Orbit Android MVI

![Logo](images/logo.png)

Orbit is an MVI framework for Android we use at [Babylon Health](https://www.babylonhealth.com).

## Why Orbit

Orbit provides the minimum structure possible around your redux implementation
to make it easy to use, yet leave you open to use RxJava's power.

When we created Orbit, we initially looked at other redux libraries out there
but felt they didn't meet our needs. Some didn't handle Android lifecycle, and
others had elaborate structured APIs while some provided custom functionality
that RxJava gives you out of the box.

We drew inspiration from [Managing State with RxJava by Jake Wharton](https://www.reddit.com/r/androiddev/comments/656ter/managing_state_with_rxjava_by_jake_wharton/),
[RxFeedback](https://github.com/NoTests/RxFeedback.kt) and [Mosby MVI](https://github.com/sockeqwe/mosby).

For more details about MVI and our implementation, read our [overview](docs/overview.md).

## Getting started

Include the following dependencies in your build.gradle.kts file:

```kotlin
implementation("com.babylon.orbit:orbit:<latest-version>")
implementation("com.babylon.orbit:orbit-android:<latest-version>")
```

A real-world redux system might look as follows:

``` kotlin
data class State(val total: Int = 0)

data class AddAction(val number: Int)

class CalculatorMiddleware: Middleware<State, Unit> by middleware(State(), {

    perform("addition")
        .on<AddAction>()
        .withReducer { state, action ->
            state.copy(state.total + action.number)
        }
})
```

Read more about what makes an [orbit](docs/orbits.md).

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md)
for details on our code of conduct, and the process for submitting pull
requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions
available, see the [tags on this repository](https://github.com/Babylonpartners/orbit-android-mvi/tags).

## License

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE.md)

This project is licensed under the Apache License, Version 2.0 - see the
[LICENSE.md](LICENSE.md) file for details
