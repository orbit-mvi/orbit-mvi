---
sidebar_label: Core
---

import CodeBlock from "@theme/CodeBlock";
import latestRelease from "@site/src/plugins/github-latest-release/generated/data.json";

# Core

This is the core module for the Orbit framework.
It provides all the basic parts of Orbit.

You will need this module (or modules that include it) to get started!

<CodeBlock language="kotlin">implementation("org.orbit-mvi:orbit-core:{latestRelease.tag_name}")</CodeBlock>

See [architecture](architecture.md) if you're interested in learning more about
MVI and how its concepts map onto Orbit's components.

## Orbit container

A
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
is the heart of the Orbit MVI system. It retains the state, allows you to listen
to side effects and state updates and allows you to modify the state through the
`orbit` function which executes Orbit operators of your desired business logic.

### Subscribing to the container

[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
exposes flows that emit updates to the container state and side effects.

- State emissions are conflated
- Side effects are cached by default if no observers are listening. This
  can be changed via
  [Settings Builder](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-settings-builder/)

```kotlin
data class ExampleState(val seen: List<String> = emptyList())

sealed class ExampleSideEffect {
    data class Toast(val text: String)
}

class ExampleContainerHost(scope: CoroutineScope) : ContainerHost<ExampleState, ExampleSideEffect> {
    
    // create a container
    override val container = scope.container<ExampleState, ExampleSideEffect>(ExampleState())

    fun doSomethingUseful() = intent {
        ...
    }
}

private val scope = CoroutineScope(Dispatchers.Main)
private val viewModel = ExampleContainerHost(scope)

fun main() {

    // subscribe to updates
    // On Android, use ContainerHost.observe() from the orbit-viewmodel module
    scope.launch {
        viewModel.container.stateFlow.collect {
            // do something with the state
        }
    }
    scope.launch {
        viewModel.container.sideEffectFlow.collect {
            // do something with the side effect
        }
    }

    viewModel.doSomethingUseful()
    
    // Ensure the main function does not complete so we can do something useful with the container.
}
```

### ContainerHost

A
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
is not strictly required to work with an Orbit
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/).
However, Orbit's syntax is defined as an extension on this class. Additionally
it simplifies and organises your business logic and so is highly recommended. A
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
typically defines MVI flows (your business logic and Orbit operators to be
invoked on the
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/))
as functions that can be called by e.g. the UI.

In a typical implementation you would subclass Android's `ViewModel` and
implement
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
in order to create an Orbit-enabled Android `ViewModel`.

```kotlin
class ExampleViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel(), ContainerHost<ExampleState, ExampleSideEffect> {
    // create a container
    val container = container<ExampleState, ExampleSideEffect>(ExampleState(), savedStateHandle)

    …
}
```

## Core operators

The Core module contains built-in Orbit operators. Here's how
they map to MVI concepts:

| MVI Operation      | Orbit DSL                      | Purpose                                                                                                   |
|--------------------|--------------------------------|-----------------------------------------------------------------------------------------------------------|
| block              | `intent { ... }`               | Contains business logic, allows you to invoke other operators within                                      |
| transformation     | operations within `intent`     | Run business operations to transform data                                                                 |
| posted side effect | `postSideEffect(...)`          | Sends one-off events to the side effect channel                                                           |
| reduction          | `reduce { ... }`               | Atomically updates the Container's  state                                                                 |
| -                  | `repeatOnSubscription { ... }` | Helps collect infinite flows only when there are active subscribers                                       |
| -                  | `subIntent { ... }`            | Use this to break big `intent` blocks into smaller parts, or for parallel decomposition.                  |
| -                  | `runOn { ... }`                | Useful for working with sealed class states. The block is only executed if (and while) the state matches. |

Operators are invoked through the `intent` block in a
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/).
For more information about which threads these operators run on please see
[Threading](#threading).

### Transformation

```kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun simpleExample() = intent {
        anotherApiCall(apiCall()) // just call suspending functions
    }
}
```

Transformations change upstream data into a different type. Transformations
can do a simple mapping or something more complex like call a backend API or
subscribing to a `Flow`.

:::tip

Infinite `Flow`s are best collected within a
[repeatOnSubscription](#repeat-on-subscription) block.

:::

In Orbit, the transformations are simply suspend function calls in the block
function. It is your responsibility to ensure you are using the correct
context for your calls. Blocking code in your `intent` block will generally
cause Orbit's "event loop" to be blocked, effectively preventing processing
of new intents until that code completes.

### Reduction

```kotlin
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

```kotlin
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
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)'s
side effect flow. Use this for view-related side effects like Toasts,
Navigation, etc.

Side effects are cached if there are no observers, guaranteeing critical
events such as navigation are delivered after re-subscription.

:::caution

`Container.sideEffectFlow` is designed for a single observer to ensure
predictable side effect caching. If you need multiple observers, use `shareIn`,
but note that caching may not apply to the resulting `SharedFlow`.

:::

### Repeat on subscription

```kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun simpleExample() = intent(idlingResource = false) {
        repeatOnSubscription {
            expensiveFlow().collect {
                //
            }
        }
    }
}
```

Collecting flows directly in an `intent` block continues until the flow
completes or cancels. Cancellation happens automatically when the Orbit
coroutine scope cancels.

The lifecycle of the Orbit coroutine scope, especially when set to
`viewModelScope` may outlive the lifecycle of the UI resulting in subscriptions
continuing in the background.

For expensive subscriptions, such as location or Bluetooth, this may be
undesirable, you only want to collect from the flow when the UI actively
observes the state or sideEffect streams.

`repeatOnSubscription` provides functionality to start (and restart) its inner
block when the state or sideEffect streams are being observed and stop when that
is no longer the case.

### Sub-intent

```kotlin
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    ...
    
    override val container = scope.container<ExampleState, ExampleSideEffect>(
        ExampleState()
    ) {
        // Remember that this `onCreate` block is an implicit `intent`.
        // A common pattern is to subscribe to various subintents collecting
        // flows or other work here.
        coroutineScope {
            launch { smallIntent() }
            launch { smallIntent2() }
            launch { collectFlow() }
        }
    }

    fun largeIntent() = intent {
        smallIntent()
        smallIntent2()
    }

    fun largeIntentWithParallelDecomposition() = intent {
        coroutineScope {
            launch { smallIntent() }
            launch { smallIntent2() }
        }
    }
    
    suspend fun smallIntent() = subIntent {
        // ... do work
        reduce { state.copy(...) }
    }

    suspend fun smallIntent2() = subIntent {
        // ... do work
        reduce { state.copy(...) }
    }
    
    suspend fun collectFlow() = subIntent {
        someFlow.collect {
            // ... do work
            reduce { state.copy(...) }
        }
    }
}
```

Think of `subIntent` as a suspend function, but with access to Orbit syntax.

It is useful for breaking down large `intent` blocks into smaller parts, or for
parallel decomposition of work.

Most commonly, you would combine several `subIntent` calls in a single 
`onCreate` block of the container factory. This is a common pattern when 
collecting several flows or performing several parallel operations on creation.

### RunOn

```kotlin
class Example : ContainerHost<ExampleSealedClassState, ExampleSideEffect> {
    ...
    
    override val container = 
        scope.container<ExampleSealedClassState, ExampleSideEffect>(
            ExampleSealedClassState.Loading
        )

    fun withoutRunOn() = intent {
        state as? ExampleSealedClassState.Ready?.let {
            ... run some operations with the captured state
        }
    }

     fun withRunOn() = intent {
        runOn(ExampleSealedClassState.Ready::class) {
            ... run some operations with the captured state
        }
    }
}
```

`runOn` is a helper DSL to improve working with sealed class states. It allows
you to run a block of code only when the state matches the provided type and
optionally the predicate.

Additionally, the block is automatically cancelled when the state no longer 
matches the provided type or predicate.

Note that there are no atomicity guarantees when using `runOn`. The block may
get executed partially.

### Operator context

Each simple syntax operator lambda has a receiver that exposes the current state
of the
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
as `state`

```kotlin
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

:::note

`reduce` is a special operator, where state is captured when its lambda is
invoked. This means that within a `reduce` block, your state is guaranteed
not to change.

:::

## Container factories

```kotlin
perform("Toast the current state")
class Example : ContainerHost<ExampleState, ExampleSideEffect> {
    override val container = container<ExampleState, ExampleSideEffect>(ExampleState()) {
        // This block is an intent invoked when the container is first created
        reduce { ... }
    }
}
```

Containers are typically not created directly but through convenient factory
functions. This allows you to pass through extra settings or an intent lambda to
invoke when the
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
is first created (important for containers that can be recreated from a saved
state or live longer than the UI).

A typical use case for this is to collect `Flow`s that we need to start
observing right after the container is created.

```kotlin
perform("Toast the current state")
class Example(
    private val flow1: Flow<Int>,
    private val flow2: Flow<Int>,
): ContainerHost<ExampleState, ExampleSideEffect> {
    override val container = container<ExampleState, ExampleSideEffect>(ExampleState()) {
        coroutineScope {
            repeatOnSubscription {
            launch {
                flow1.collect {
                    reduce { ... }
                }
            launch {
                flow2.collect {
                    reduce { ... }
                }
            }
        }
    }
}
```

Extra
[Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
factory functionality is provided via extension functions. One example is
`ViewModel` saved state support via a `SavedStateHandle`.

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
[Settings Builder](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-settings-builder/)
property `exceptionHandler` -> if defined exceptions are caught here so
parent scope is not affected and Orbit container
would continue to operate normally.
