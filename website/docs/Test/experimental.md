---
sidebar_position: 2
sidebar_label: 'Experimental testing process'
---

# Experimental testing process

We are currently working on a new testing framework for Orbit MVI. The new
framework is still in the experimental phase and we are looking for feedback.

The new framework is based on the (itself in
alpha) [Turbine](https://github.com/cashapp/turbine)
library. Turbine is a library for testing coroutines and flows.

Orbit's experimental framework offers a subset of the Turbine APIs and
ensures predictable coroutine scoping and context through use of the new
[coroutine testing APIs](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/kotlinx.coroutines.test/run-test.html)
.

### Current status

- We have a working prototype of the new testing framework. We will be testing
  it within a real project and will be making changes based on community
  feedback.
- The API is still subject to change. Notably we may be missing some convenience
  functions in
  the [OrbitTestContext](pathname:///dokka/orbit-test/org.orbitmvi.orbit.test/-orbit-test-context/)
- The framework will remain experimental
  until [Turbine](https://github.com/cashapp/turbine)
  is stable.

## Changes from the current testing framework

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
4. (Optional) Run `invokeIntent { foo() }` to run the
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
        invokeIntent { countToFour() }

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

Note: `runOnCreate` can only be invoked once and before `invokeIntent`:

```kotlin
@Test
fun exampleTest() = runTest {
        ExampleViewModel().test(this) {
            runOnCreate() // may be invoked only once and before `invokeIntent`
            expectInitialState()
            invokeIntent { countToFour() }
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
            invokeIntent { countToFour() }

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
            invokeIntent { countToFour() }

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
            invokeIntent { countToFour() }

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

## Testing Flows

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

We should ideally replace the infinite flow with a finite flow for the test.
This helps keep the test simple. If this is not possible, we have to make
sure we disregard extra states and side effects that are emitted at the end
of the test by calling `cancelAndIgnoreRemainingItems()`.

Otherwise, testing a container that subscribes to an infinite flow is no
different to normal testing.

```kotlin
@Test
fun exampleTest() = runTest {
        // Fake returning a cold, finite flow.
        val fakeLocationService = FakeLocationService()

        ExampleViewModel(fakeLocationService).test(this) {
            expectInitialState()
            runOnCreate()

            expectState { copy(lng = 1, lat = 1) }
            expectState { copy(lng = 2, lat = 2) }
            expectState { copy(lng = 3, lat = 3) }
            
            // If the flow is infinite, we must call this to ignore the unconsumed items
            // No need to call this for a finite flow
            // cancelAndIgnoreRemainingItems()
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
            invokeIntent { incrementForever() }

            // Assert the first three states
            expectState(listOf(42, 43))
            expectState(listOf(42, 43, 44))
            expectState(listOf(42, 43, 44, 45))

            // If the flow is infinite, we must call this to ignore the unconsumed items
            // No need to call this for a finite flow
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
            invokeIntent { incrementForever() }

            // Assert the first three states
            scope.advanceTimeBy(30_001)
            expectState(listOf(42, 43))
            scope.advanceTimeBy(30_001)
            expectState(listOf(42, 43, 44))
            scope.advanceTimeBy(30_001)
            expectState(listOf(42, 43, 44, 45))

            // No need to call `cancelAndIgnoreRemainingItems()` since
            // We control virtual time
        }
    }
```
