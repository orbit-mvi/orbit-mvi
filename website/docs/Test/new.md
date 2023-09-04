---
sidebar_position: 2
sidebar_label: 'New testing process'
---

# New testing process

The new framework is based on the (itself in
alpha) [Turbine](https://github.com/cashapp/turbine)
library. Turbine is a library for testing coroutines and flows.

Orbit's framework offers a subset of the Turbine APIs and
ensures predictable coroutine scoping and context through use of the new
[coroutine testing APIs](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/kotlinx.coroutines.test/run-test.html)
.

## Changes from the old testing framework

Our design goal was to have a simpler API and no more hidden magic in tests.

- The new testing framework is available under the `org.orbitmvi.orbit.test`
  package.
- Flow isolation is gone. If you have any loopbacks in your intents, you will
  need to test them explicitly.
- Removed the orbit-specific assertions. You are now free to use any assertion
  library you like.
- Gone are the two testing modes - suspending and live. The new framework
  always runs a real Orbit container and uses the `TestScope`'s background
  dispatcher. We no longer see a need to have both modes, as it often led to
  confusion and bad practices.
- The framework is a light wrapper
  around [Turbine](https://github.com/cashapp/turbine),
  while also adding some Orbit-specific functionality and ensuring we use the
  correct coroutine context to have predictable tests and proper cleanup.
- Side effects and states must be awaited for in order. Previously, states and
  side effects were asserted separately, which meant we were not testing the
  order in which they were emitted.

## Testing process

1. Put the
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   in your chosen test mode using `test()`. You may optionally
   provide them with the initial state to seed the container with. This helps
   avoid having to call several intents just to get the container in the right
   state for the test.
2. Assert the initial state using `expectInitialState()`.
3. (Optional) Run `runOnCreate()` within the test block to run the container
   create lambda.
4. (Optional) Run `containerHost.foo()` to run the
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   intent of your choice.
5. Await for side effects and states using `awaitSideEffect()`
   and `awaitState()`.
   `testContainerHost.assert { ... }`.

Let's start and put our
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
into test mode. We pass in the test scope and initial state to seed the
container with (you may omit it entirely to use the initial state from the real
container).

Next, it is suggested to assert the initial state. This is a sanity check to
ensure that the container is in the correct state before we start testing.

We provide a convenience function `expectInitialState()` for this purpose.

After that, we can invoke intents on the container to continue testing.

```kotlin
data class State(val count: Int = 0)

@Test
fun exampleTest() = runTest {
    ExampleViewModel().test(this, State()) {
        expectInitialState()
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
            expectInitialState()
            containerHost.countToFour()
        }
    }
```

### Asserting states

Having done the above, we can move to assertions. The initial state has to be
explicitly asserted first, as a sanity check.

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            expectInitialState()
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

### Asserting posted side effects

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            expectInitialState()
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
            expectInitialState()
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
            expectInitialState()
            
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
            expectInitialState()
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
            expectInitialState()
            
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
            expectInitialState()
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
            expectInitialState()
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
            expectInitialState()
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
