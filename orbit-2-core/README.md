# Orbit 2 Core

This is the core module for the Orbit 2 framework.
It provides all the basic parts of Orbit.

- [Orbit 2 Core](#orbit-2-core)
  - [Architecture](#architecture)
    - [Orbit concepts](#orbit-concepts)
    - [Side effects](#side-effects)
  - [Including the module](#including-the-module)
  - [Orbit container](#orbit-container)
    - [Subscribing to the container](#subscribing-to-the-container)
    - [ContainerHost](#containerhost)
  - [Core Orbit operators](#core-orbit-operators)
    - [Transform](#transform)
    - [Reduce](#reduce)
    - [Side effect](#side-effect)
    - [Operator context](#operator-context)
  - [Container factories](#container-factories)
  - [Threading](#threading)
    - [Threading guarantees](#threading-guarantees)
  - [Error handling](#error-handling)

## Architecture

![Orbit 2 overview 1](docs/orbit-2-overview-1.svg)

This diagram shows a simple representation of how an Orbit system (or similar
systems like MVI/Redux/Cycle) works in simple principles.

1. The UI sends actions asynchronously to a business component.
1. The business component transforms the incoming events with business logic
1. The business component then emits these events further down the chain
1. Every event is reduced with the current state of the system to produce a new
   state
1. The state is then emitted back to the UI which renders itself based upon
   information within

The main thing to remember is that the UI cannot make any business decisions
by itself. It should know only how to render itself based on the input state.

### Orbit concepts

![Orbit 2 overview 2](docs/orbit-2-overview-2.svg)

We can map the above logic onto real components.

1. UI invokes functions on a class implementing the
   [ContainerHost](src/main/java/com/babylon/orbit2/ContainerHost.kt) interface.
   Typically in Android this might be an Activity, Fragment or a simple View.
   However, an Orbit system can also be run without any UI, for example as a
   background service.
1. The functions call through to a
   [Container](src/main/java/com/babylon/orbit2/Container.kt) instance through
   the `orbit` block which applies Orbit operators
1. Transformer operators apply business logic that transforms function
   parameters into single or multiple events
1. The reducer operator reduces the current state of the system with the
   incoming events to produce new states
1. The new state is sent to observers

Notes:

- All Orbit operators are optional.

### Side effects

In the real world such a system cannot exist without side effects. Side effects
are commonly truly one-off events like navigation, logging, analytics, toasts
etc that do not alter the state of the Orbit
[Container](src/main/java/com/babylon/orbit2/Container.kt). As such there's a
third Orbit operator that can deal with side effects.

![Orbit 2 overview 3](docs/orbit-2-overview-3.svg)

The UI does not have to be aware of all side effects (e.g. why should the UI
care if you send analytics events?). As such you can have side effects that do
not post any event back to the UI.

## Including the module

Orbit 2 is a modular framework. You will need this module to get started!

Additional functionality is provided through optional modules.

```kotlin
implementation("com.babylon.orbit2:orbit-core:<latest-version>")
```

## Orbit container

A [Container](src/main/java/com/babylon/orbit2/Container.kt) is the heart of the
Orbit MVI system. It retains the state, allows you to listen to side effects and
state updates and allows you to modify the state through the `orbit` function
which executes Orbit operators of your desired business logic.

### Subscribing to the container

[Container](src/main/java/com/babylon/orbit2/Container.kt) exposes flows
that emit updates to the container state and side effects.

- State emissions are conflated
- Side effects are cached by default if no observers are listening. This
  can be changed via
  [Container Settings](src/main/java/com/babylon/orbit2/Container.kt#Settings)

``` kotlin
data class ExampleState(val seen: List<String> = emptyList())

sealed class ExampleSideEffect {
   data class Toast(val text: String)
}

fun main() {
    // create a container
    val container = container<ExampleState, ExampleSideEffect>(ExampleState())

    // subscribe to updates
    CoroutineScope(Dispatchers.Main).launch {
        container.stateFlow.collect {
            // do something with the state
        }
        container.sideEffectFlow.collect {
            // do something with the side effect
        }
    }
}

```

### ContainerHost

A [ContainerHost](src/main/java/com/babylon/orbit2/ContainerHost.kt) is not
strictly required to work with an Orbit
[Container](src/main/java/com/babylon/orbit2/Container.kt), but it simplifies
and organises access to it and so is highly recommended. A
[ContainerHost](src/main/java/com/babylon/orbit2/ContainerHost.kt) typically
defines Orbit flows (chains of Orbit operators to be invoked on the
[Container](src/main/java/com/babylon/orbit2/Container.kt)) as functions that
can be called by e.g. the UI.

In a typical implementation you would subclass Android's `ViewModel` and
implement [ContainerHost](src/main/java/com/babylon/orbit2/ContainerHost.kt) in
order to create an Orbit-enabled Android `ViewModel`.

## Core Orbit operators

The Core module contains built-in Orbit operators:

- transform
- sideEffect
- reduce

Transformers, side effects and reducers are invoked via simple Orbit operators.
Operators are invoked via the `orbit` function in a
[ContainerHost](src/main/java/com/babylon/orbit2/ContainerHost.kt) (or, less
commonly, a [Container](src/main/java/com/babylon/orbit2/Container.kt) directly)

For more information about which threads these operators run on please see
[Threading](#threading).

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    override val container = container<ExampleState, ExampleSideEffect>(ExampleState())

    fun example(number: Int) = orbit {
       transform {
          number.toString()
       }
         .sideEffect { post(ExampleSideEffect.Toast(event)) }
         .reduce { state.copy(seen = state.seen + event) }
    }
}
```

Aside from the three Orbit operators already mentioned, more are provided via
modules with support for e.g. `RxJava2` or Kotlin `Coroutines`. In theory Orbit
can work with any async and streaming framework - new modules can be created
easily.

### Transform

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example(number: Int) = orbit {
            transform { number * number }
        }
    }
    fun anotherExample() = orbit {
            transform { apiCall() }
                .transform { anotherApiCall(event) } // "event" is the result of the first api call
        }
    }
}
```

Transformers are akin to the `map` or `flatMap` functions recognisable from
popular stream libraries.

Their primary purpose is to change upstream data into a different type.
Transformers can do a simple mapping or do something much more complex like call
a backend API or subscribe to a stream of location updates.

### Reduce

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example(number: Int) = orbit {
        reduce { state.copy(state.total + number)}
    }

    fun anotherExample(number: Int) = orbit {
        transform { apiCall() }
            .reduce { state.copy(results = event.results) }
    }
}
```

Reducers take incoming events and the current state to produce a new state.

Reducers are pass-through operators. This means that after applying
a reducer, the upstream event is passed through unmodified to downstream
operators. This helps avoid having to create intermediate objects to retain
upstream events.

Operators downstream of a reducer can expect to be called only after the
upstream reduction has completed.

### Side effect

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example(number: Int) = orbit {
        sideEffect { trackSomething() }
    }

    fun anotherExample(number: Int) = orbit {
        transform { apiCall() }
            .sideEffect { post(ExampleSideEffect.Toast("event $event")) }
            .reduce { state.copy(results = event.results) }
    }
}
```

Working with any system will eventually generate side effects. We've made them a
first class citizen in Orbit.

This functionality is commonly used for things like truly one-off events,
navigation, logging, analytics etc.

You may use the `post` method within `sideEffect` in order to send the value to
a [Container](src/main/java/com/babylon/orbit2/Container.kt)'s side effect
stream. Use this for view-related side effects like Toasts, Navigation, etc.

Side effects are pass-through operators. This means that after applying
a side effect, the upstream event is passed through unmodified to downstream
operators. This helps avoid having to create intermediate objects to retain
upstream events.

### Operator context

Commonly in an operator you need two things:

- The current state of the [Container](src/main/java/com/babylon/orbit2/Container.kt)
- The upstream event

Each Orbit operator lambda has a receiver that exposes the above as fields:

- `state`
- `event`

**Inside an operator invocation the same state value will be returned regardless
of what may be changing it in other threads.**

Examples of using the exposed fields:

``` kotlin
perform("Toast the current state")
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun anotherExample(number: Int) = orbit {
        transform { apiCall() }
            .sideEffect { post(ExampleSideEffect.Toast("state $state")) }
            .reduce { state.copy(results = event.results) }
    }
}
```

## Container factories

``` kotlin
perform("Toast the current state")
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    override val container = container<ExampleState, ExampleSideEffect>(ExampleState()) {
        onCreate()
    }

    fun onCreate() = orbit {
        ...
    }
}
```

Containers are typically not created directly but through convenient factory
functions. This allows you to pass through extra settings or a lambda to invoke
when the [Container](src/main/java/com/babylon/orbit2/Container.kt) is first
created (important for containers that can be recreated from a saved state or
live longer than the UI).

Extra [Container](src/main/java/com/babylon/orbit2/Container.kt) factory
functionality is provided via extension functions. One example is `ViewModel`
saved state support via a `SavedStateHandle`.

## Threading

Orbit is designed to provide a sane default threading model to cater for most of
the typical use cases. That being said you are not constrained and are free to
switch threads if you need to (e.g. for database access). Typically that is
done within particular `transform` blocks e.g. `transformSuspend`.

### Threading guarantees

- Calls to `Container.orbit` do not block the caller.
- `transform` and `transformX` calls execute in an `IO` thread so as not to
  block the Orbit [Container](src/main/java/com/babylon/orbit2/Container.kt)
  from accepting further events.
- Updates delivered via `Container.stateStream` and
  `Container.sideEffectStream` come in on the main coroutine dispatcher
  if installed, with the default dispatcher as the fallback. However,
  the connection to the stream has to be manually managed and cancelled.

## Error handling

It is good practice to handle all of your errors within your flows.
Orbit does not provide any built-in exception handling because it cannot
make assumptions about how you respond to errors, avoiding putting your
system in an undefined state.
