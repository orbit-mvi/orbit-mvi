---
sidebar_label: 'Test'
---

import CodeBlock from "@theme/CodeBlock";
import latestRelease from "@site/src/plugins/github-latest-release/generated/data.json";


# Test

The framework is based on the [Turbine](https://github.com/cashapp/turbine)
library. Turbine is a library for testing coroutines and flows.

Orbit's framework offers a subset of the Turbine APIs and
ensures predictable coroutine scoping and context through use of the
[coroutine testing APIs](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/kotlinx.coroutines.test/run-test.html).

<CodeBlock language="kotlin">testImplementation("org.orbit-mvi:orbit-test:{latestRelease.tag_name}")</CodeBlock>

## Testing process

1. Put the
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   in your chosen test mode using `test()`. You may optionally
   provide them with the initial state to seed the container with. This helps
   avoid having to call several intents just to get the container in the right
   state for the test.
2. (Optional) Run `runOnCreate()` within the test block to run the container
   create lambda.
3. (Optional) Run `containerHost.foo()` to run the
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   intent of your choice.
4. Await for side effects and states using `awaitSideEffect()`
   and `awaitState()`.
   `testContainerHost.assert { ... }`.

Let's start and put our
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
into test mode. We pass in the test scope and initial state to seed the
container with (you may omit it entirely to use the initial state from the real
container).

:::note

The initial state is automatically verified, however, auto-check can be disabled with 
`settings = TestSettings(autoCheckInitialState = false)` and the state verified using `awaitState`
or `expectState`.

:::

Next, we can invoke intents on the container to continue testing.

```kotlin
data class State(val count: Int = 0)

@Test
fun exampleTest() = runTest {
    ExampleViewModel().test(this, State()) {
        containerHost.countToFour()

        // await states and side effects, perform assertions
    }
}
```

### Run `onCreate`

If the `Container` is created with `CoroutineScope.container()` or
`ViewModel.container()` there is an option to provide the `onCreate` lambda.
In test mode this function must be run manually (if needed) by calling
`runOnCreate`, so it's effectively isolated in the test; the other
reason why is `onCreate` could include any number of `intent{}` calls, so it's
crucial in terms of testing.

It is strongly suggested you avoid calling `runOnCreate()` if you are not
testing the intents called within. For other cases, it is recommended to
set a correct initial state instead.

:::note

`runOnCreate` can only be invoked once, before invoking any intents on
`ContainerHost`.

:::

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            runOnCreate()
            containerHost.countToFour()
        }
    }
```

### Asserting states

Having done the above, we can move to assertions.

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            containerHost.countToFour()

            expectState { copy(count = 1) }
            // alternatively assertEquals(State(count = 1), awaitState())
            expectState { copy(count = 2) }
            expectState { copy(count = 3) }
            expectState { copy(count = 4) }
        }
    }
```

If any unconsumed items (states or side effects) are left at the end of the
test, it will fail. All items must be consumed before the test ends. This is to
ensure no unwanted extra states or side effects are emitted.

#### Sealed class/interface states

If you model state as a sealed class/interface, you can use the `expectStateOn`
function to avoid type-casting when asserting changes.

```kotlin
sealed interface State {
    data object Loading : State
    data class Ready(val count: Int): State
}

@Test
fun exampleTest() = runTest {
    ExampleViewModel().test(this) {
        containerHost.countToTwo()

        expectState { State.Ready(count = 1) }
        // Use expectStateOn to avoid type-casting, this is equivalent to
        //   expectState { (this as State.Ready).copy(count = 2) }
        expectStateOn<State.Ready> { copy(count = 2) }
    }
}
```

### Asserting posted side effects

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            containerHost.countToFour()

            expectSideEffect(Toast(1))
            expectSideEffect(Toast(2))
            expectSideEffect(Toast(3))
            expectSideEffect(Toast(4))
        }
    }
```

### Putting it all together

Here's what it looks like once we put it together.

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            runOnCreate()
            containerHost.countToFour()

            expectState { copy(count = 1) }
            expectSideEffect(Toast(1))
            expectState { copy(count = 2) }
            expectSideEffect(Toast(2))
            expectState { copy(count = 3) }
            expectSideEffect(Toast(3))
            expectState { copy(count = 4) }
            expectSideEffect(Toast(4))
        }
    }
```

## Intent Jobs

If your intent does not produce any states or side effects, but e.g. affects an
external dependency, you need to make sure the intent completes before running
your assertions.

This can be done using coroutine
[Jobs](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/index.html)
, which are returned by `runOnCreate` or `containerHost.foo()`.


```kotlin
@Test
fun exampleTest() = runTest {
        val dependency = SomeDependency()
        
        ExampleViewModel(dependency).test(this) {
            val job = runOnCreate()
            // OR
            val job = containerHost.doSomeWorkOnDependency()
            
            // Ensure intent is completed
            job.join()
            
            // Run your assertions
            assertEquals(dependency.counter, 42)
        }
    }
```

## Additional checks and assertions

In unit testing, it is the things we don't test for that can cause the most
unexpected bugs. In order to bring this into focus, Orbit's test framework 
requires you to be very explicit. The only time we can be sure the test is
complete is when the Orbit container is at rest and all states and side effects
have been inspected.

Below are some additional checks we perform to make sure this is the case.

### Unconsumed states or side effects

If there are any unconsumed states or side effects at the end of the test, it
will fail.

This is to ensure you check all the states and side effects that are the result
of your intent calls - and that you don't end up in a state you didn't expect.

Ideally you would assert on all states and side effects, but if this is not
convenient, you can use `skip(n)` or `cancelAndIgnoreRemainingItems()` to
explicitly mark that you are not interested in testing the remaining items.
Typically, `cancelAndIgnoreRemainingItems` would be used as a last resort.

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            runOnCreate()
            containerHost.countToFour()

            expectState { copy(count = 1) }
            expectSideEffect(Toast(1))
            
            // Deal with unconsumed items that were emitted by the container
            skip(4)
            // OR ignore all unconsumed items
            cancelAndIgnoreRemainingItems()
        }
    }
```

### Unfinished intents

Any intents that are still running at the end of the test will cause the test to
fail.

This is to ensure that the container can't emit any more states or side effects
after the test has finished.

Typically, this is caused by an intent subscribing to a flow that never
completes or launching a long-running, blocking intent.

In order to complete the test successfully in these circumstances, the intent
must be joined or cancelled.

See below example for options to deal with this. Typically, 
`cancelAndIgnoreRemainingItems` would be used as a last resort.

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            val job = runOnCreate()
            // OR
            val job = containerHost.doSomeWork()
            
            // ... run assertions
            
            // Ensure intent is completed
            job.join()
            // OR cancel the intent
            job.cancel()
            // OR cancel all intents
            cancelAndIgnoreRemainingItems()
        }
    }
```

## Testing intents that collect Flows

We can run into situations where we subscribe
our [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
to an infinite (hot) flow of data like so:

```kotlin
val container = scope.container<SomeState, Unit> {
    intent {
        runOnSubscription {
            locationService.locationUpdates.collect {
                reduce { state.copy(lng = it.lng, lat = it.lat) }
            }
        }
    }
}
```

A good practice is to replace the infinite flow with a finite flow for the test.
This helps keep the test simple. 

If this is not possible or desirable, we may run the intent collecting the
infinite flow as normal and `join()` the `Job` returned by the intent to ensure
it is completed at the end of the test. 

Our last resort is calling `cancelAndIgnoreRemainingItems()` to cancel the scope
and disregard any extra states and side effects that are emitted at the end
of the test.

Otherwise, testing a container that subscribes to an infinite flow is no
different to normal testing.

```kotlin
@Test
fun exampleTest() = runTest {
        // Fake returning a cold, finite flow.
        val fakeLocationService = FakeLocationService()

        ExampleViewModel(fakeLocationService).test(this) {
            val job = runOnCreate()

            expectState { copy(lng = 1, lat = 1) }
            expectState { copy(lng = 2, lat = 2) }
            expectState { copy(lng = 3, lat = 3) }
            
            // If the flow is infinite, we must ensure the intent is finished
            // at the end of the test.
            job.join()
            // OR
            cancelAndIgnoreRemainingItems()
        }
    }
```

## Control over virtual time

By default, and by virtue of running within the `runTest` block internally,
the tests will skip any delays. When this is not desirable and we want granular
control over virtual time, we need to create a separate `TestScope` and pass it
to the test function.

Consider the following example:

```kotlin
class InfiniteFlowMiddleware : ContainerHost<List<Int>, Nothing> {
    override val container: Container<List<Int>, Nothing> = someScope.container(listOf(42))

    fun incrementForever() = intent {
        while (true) {
            delay(30_000)
            reduce { state + (state.last() + 1) }
        }
    }
}
```

### With delay skipping

Testing with delay skipping is the default behaviour. This is the same as any
coroutine being tested in `runTest`.

```kotlin
@Test
fun delaySkipping() = runTest {
        InfiniteFlowMiddleware().test(this) {
            val job = containerHost.incrementForever()

            // Assert the first three states
            expectState(listOf(42, 43))
            expectState(listOf(42, 43, 44))
            expectState(listOf(42, 43, 44, 45))

            // If the flow is infinite, we must ensure the intent is finished
            // at the end of the test.
            job.join
            // OR
            cancelAndIgnoreRemainingItems()
        }
    }
```

### Without delay skipping

If we wish to control the virtual time, we must create a separate `TestScope`
and pass it to the container.

```kotlin
@Test
fun noDelaySkipping() = runTest {
        val scope = TestScope()

        InfiniteFlowMiddleware().test(scope) {
            val job = containerHost.incrementForever()

            // Assert the first three states
            scope.advanceTimeBy(30_001)
            expectState(listOf(42, 43))
            scope.advanceTimeBy(30_001)
            expectState(listOf(42, 43, 44))
            scope.advanceTimeBy(30_001)
            expectState(listOf(42, 43, 44, 45))

            // If the flow is infinite, we must ensure the intent is finished
            // at the end of the test.
            job.join
            // OR
            cancelAndIgnoreRemainingItems()
        }
    }
```

## Compose UI Testing

See [Compose UI Testing](../Compose/#compose-ui-testing) for more details.
