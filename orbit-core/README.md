---
id: overview
sidebar_label: Overview
---

# Core

This is the core module for the Orbit framework.
It provides all the basic parts of Orbit.

- [Core](#core)
  - [Architecture](#architecture)
    - [Orbit concepts](#orbit-concepts)
    - [Side effects](#side-effects)
      - [Limitations](#limitations)
  - [Including the module](#including-the-module)
  - [Orbit container](#orbit-container)
    - [Subscribing to the container](#subscribing-to-the-container)
    - [ContainerHost](#containerhost)
  - [Core Orbit operators](#core-orbit-operators)
    - [Transformation](#transformation)
    - [Reduction](#reduction)
    - [Side effect](#side-effect)
    - [Operator context](#operator-context)
  - [Container factories](#container-factories)
  - [Threading](#threading)
    - [Threading guarantees](#threading-guarantees)
  - [Error handling](#error-handling)

## Architecture

![Orbit overview 1](docs/orbit-overview-1.svg)

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

![Orbit overview 2](docs/orbit-overview-2.svg)

We can map the above logic onto real components.

1. UI invokes functions on a class implementing the
   [ContainerHost](src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)
   interface. Typically in Android this might be an Activity, Fragment
   or a simple View. However, an Orbit system can also be run without
   any UI, for example as a background service.
1. The functions call through to a
   [Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt) instance
   through the `intent` block which offloads work to a background coroutine
   and provides a DSL for side effects and reductions.
1. Transformations are performed through user-defined business logic within
   the `intent` block.
1. The reduce operator reduces the current state of the system with the
   incoming events to produce new states.
1. The new state is sent to observers.

Notes:

- All Orbit operators are optional.

### Side effects

In the real world such a system cannot exist without side effects. Side effects
are commonly truly one-off events like navigation, logging, analytics, toasts
etc that do not alter the state of the Orbit
[Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt). As such
there's a third Orbit operator that can deal with side effects.

![Orbit overview 3](docs/orbit-overview-3.svg)

The UI does not have to be aware of all side effects (e.g. why should the UI
care if you send analytics events?). As such you can have side effects that do
not post any event back to the UI.

Side effects are cached if there are no observers, guaranteeing critical
events such as navigation are delivered after re-subscription.

#### Limitations

`Container.sideEffectFlow` is designed to be collected by only one
observer. This ensures that side effect caching works in a predictable
way. If your particular use case requires multi-casting use `broadcast`
on the side effect flow, but be aware that caching will not work for the
resulting `BroadcastChannel`.

## Including the module

Orbit is a modular framework. You will need this module to get started!

Additional functionality is provided through optional modules.

```kotlin
implementation("org.orbit-mvi:orbit-core:<latest-version>")
```

## Orbit container

A [Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt) is the
heart of the Orbit MVI system. It retains the state, allows you to
listen to side effects and state updates and allows you to modify the
state through the `orbit` function which executes Orbit operators of
your desired business logic.

### Subscribing to the container

[Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt) exposes flows
that emit updates to the container state and side effects.

- State emissions are conflated
- Side effects are cached by default if no observers are listening. This
  can be changed via
  [Container Settings](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt#Settings)

``` kotlin
data class ExampleState(val seen: List<String> = emptyList())

sealed class ExampleSideEffect {
   data class Toast(val text: String)
}

fun main() {
    // create a container
    val container = container<ExampleState, ExampleSideEffect>(ExampleState())

    // subscribe to updates
    // For Android, use `lifecycleScope.launchWhenCreated` instead
    CoroutineScope(Dispatchers.Main).launch {
        container.stateFlow.collect {
            // do something with the state
        }
    }
    // For Android, use `lifecycleScope.launch` and `.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)` instead
    CoroutineScope(Dispatchers.Main).launch {
        container.sideEffectFlow.collect {
            // do something with the side effect
        }
    }
}

```

### ContainerHost

A [ContainerHost](src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt) is
not strictly required to work with an Orbit
[Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt). However,
Orbit's syntax is defined as an extension on this class. Additionally it
simplifies and organises your business logic and so is highly recommended. A
[ContainerHost](src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)
typically defines MVI flows (your business logic and Orbit operators to be
invoked on the
[Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)) as
functions that can be called by e.g. the UI.

In a typical implementation you would subclass Android's `ViewModel` and
implement
[ContainerHost](src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt) in
order to create an Orbit-enabled Android `ViewModel`.

## Core Orbit operators

The Core module contains built-in Orbit operators. Here's how
they map to MVI concepts:

| MVI Operation       | Orbit DSL                     |
|---------------------|-------------------------------|
| block               | `intent { ... }`              |
| transformation      | operations within `intent`    |
| posted side effect  | `postSideEffect(...)`         |
| reduction           | `reduce { ... }`              |

Operators are invoked through the block function in a
[ContainerHost](src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt). For
more information about which threads these operators run on please see
[Threading](#threading).

### Transformation

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun simpleExample() = intent {
        anotherApiCall(apiCall()) // just call suspending functions
    }
}
```

Transformations change upstream data into a different type. Transformations
can do a simple mapping or something more complex like call a backend API or
subscribing to a `Flow` of location updates.

In Orbit, the transformations are simply suspend function calls in the block
function. It is your responsibility to ensure you are using the correct
context for your calls. Blocking code in your `intent` block will generally
cause Orbit's "event loop" to be blocked, effectively preventing processing
of new intents until that code completes.

### Reduction

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun simpleExample(number: Int) = intent {
        val result = apiCall()
        reduce { state.copy(results = result) }
    }
}
```

Reducers take incoming events and the current state to produce a new state.

### Side effect

``` kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun simpleExample(number: Int) = intent {
        val result = apiCall()
        postSideEffect(ExampleSideEffect.Toast("result $result"))
        reduce { state.copy(results = result) }
    }
}
```

Working with any system will eventually generate side effects. We've made them a
first class citizen in Orbit.

This functionality is commonly used for things like truly one-off events,
navigation, logging, analytics etc.

You may post the side effect in order to send it to a
[Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)'s side
effect flow. Use this for view-related side effects like Toasts, Navigation,
etc.

### Operator context

Each simple syntax operator lambda has a receiver that exposes the
current state of the
[Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt) as `state`

``` kotlin
perform("Toast the current state")
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun anotherExample(number: Int) = intent {
        val result = apiCall()
        postSideEffect(ExampleSideEffect.Toast("state $state"))
        reduce { state.copy(results = event.results) }
    }
}
```

`reduce` is a special operator, where state is captured when it's lambda is
invoked. This means that within a `reduce` block, your state is guaranteed
not to change.

## Container factories

``` kotlin
perform("Toast the current state")
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    override val container = container<ExampleState, ExampleSideEffect>(ExampleState()) {
        onCreate()
    }

    fun onCreate() = intent {
        ...
    }
}
```

Containers are typically not created directly but through convenient factory
functions. This allows you to pass through extra settings or a lambda to invoke
when the [Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt) is
first created (important for containers that can be recreated from a saved
state or live longer than the UI).

Extra [Container](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt) factory
functionality is provided via extension functions. One example is `ViewModel`
saved state support via a `SavedStateHandle`.

## Threading

Orbit is designed to provide a sane default threading model to cater for most of
the typical use cases. That being said you are not constrained and are free to
switch threads if you need to (e.g. for database access). Typically that is
done by switching your coroutine context.

### Threading guarantees

- Calls to Container.intent` do not block the caller. The
  operations within are offloaded to an event-loop style background coroutine.
- Generally it is good practice to make sure long-running operations are done
  in a switched coroutine context in order not to block the Orbit "event
  loop".

## Error handling

It is good practice to handle all of your errors within your intents.
By default Orbit doesn't handle or process any exceptions because it cannot
make assumptions about how you respond to errors. However you could install
default exception handler via
[Container Settings](src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)
property `orbitExceptionHandler` -> if defined exceptions are caught here so
parent scope is not affected and Orbit container
would continue to operate normally.
