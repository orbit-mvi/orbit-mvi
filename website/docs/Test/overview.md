---
sidebar_position: 1
sidebar_label: 'Overview'
---

# Unit Testing module

:::caution

This framework is now **deprecated**. It will be removed in Orbit version 7.0.0.

Use the [new framework](new.md) instead.

:::

This module provides a simple unit testing framework for your Orbit
[ContainerHosts](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/).

```kotlin
testImplementation("org.orbit-mvi:orbit-test:<latest-version>")
```

## Testing goals

Experience with [Orbit 1](https://github.com/babylonhealth/orbit-mvi/blob/main/history.md) has taught us what works and what doesn't. This helped
us put constraints around our tests that we hope will make your tests
predictable and easy to write and maintain.

The testing methodology adopted here conforms to the typical testing goals of
MVI.

Concepts that we consider important to test:

- Emitted states
- Emitted side effects
- Loopbacks i.e. intent A calling intent B
- Dependencies being called

The last two items on the list are outside of the scope of this library and can
be easily tested using a mocking framework like `mockito`.

For the first two items we have created utilities that should make them easy to
test. The framework follows the Arrange/Act/Assert methodology.

### Test modes

The testing framework adds two testing modes for your
[ContainerHosts](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/).
Below is a quick summary of what they are and what are the benefits and
downsides.

1. **Suspending test mode** is the default test mode. Use it by calling
   `ContainerHost.test()`. In this mode we focus on testing the business logic
   in your `ContainerHost` by running the intercepted intents directly in the
   test as simple suspending functions. 
   - Tests must run in a coroutine - e.g. `runTest`
   - Tests circumvent the Orbit dispatching/threading mechanisms completely. We 
     believe there is no benefit to gain from running on a live container for
     most of your code. Orbit is well unit-tested, so there's no point in
     testing the framework along with your business logic.
   - Pitfalls inherent in testing a multi-threaded system are avoided
   - Assertions run instantly after all intents called are processed
   - Your tests fail fast
   - Testing infinite flows can be more difficult. See [Testing Flows](#testing-flows).
   - By default this mode isolates the first intent called on the
     [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
     Isolating intents helps avoid unexpected state/side effect emissions from
     loopbacks in your intent under test. This can be turned off if you have a
     particular testing need.
2. **Live test mode** is an alternative test mode. Use it by calling
   `ContainerHost.liveTest()`. This is recommended for more complex scenarios
   that might be difficult to test in suspending mode.
   - Tests run on a normal Orbit [Container](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container/)
     with `Unconfined` dispatcher set by default.
   - Assertions await for emissions with a timeout
   - Your tests may take some time to fail e.g. if awaiting for a missing
     emission
   - Testing infinite flows can be easier. See [Testing Flows](#testing-flows).

Other than that both test modes are very similar in terms of how you actually
write the tests.

## Testing process

Here's the testing process for both test modes:

1. Put the [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   in your chosen test mode using `test()` or `liveTest()`. You may optionally
   provide them with the initial state to seed the container with. This helps
   avoid having to call several intents just to get the container in the right
   state for the test.
2. (Optional) Run `testContainerHost.runOnCreate()` to run the container create
   lambda.
3. (Optional) Run `testContainerHost.testIntent { foo() }` to run the 
   [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
   intent of your choice.
4. Run assertions on states and side effects using
   `testContainerHost.assert { ... }`.

Let's start and put our
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
into test mode. We pass in the initial state to seed the container with (or omit
it entirely to use the initial state from the real container). Next, we call our
intent method under test.

```kotlin
data class State(val count: Int = 0)

val testSubject = ExampleViewModel().test(State())

testSubject.testIntent { countToFour() }
```

### Run `onCreate`

If the `Container` is created with `CoroutineScope.container()` or
`ViewModel.container()` there is an option to provide the `onCreate` lambda.
In test mode this function has to be run manually (if needed)
by calling `runOnCreate`, so it's effectively isolated in the test; the other
reason why is `onCreate` could include any number of `intent{}` calls, so it's
crucial in terms of testing.

Note: `runOnCreate`
should only be invoked once and before any `testIntent` call:

```kotlin
val testSubject = ExampleViewModel().test(State())

testSubject.runOnCreate() // must be invoked once and before `testIntent`
testSubject.testIntent { countToFour() }
```

### Asserting states

Having done the above, we can move to assertions. The initial state has to be
explicitly asserted first, as a sanity check.

```kotlin
testSubject.assert(State()) {
    states(
        { copy(count = 1) },
        { copy(count = 2) },
        { copy(count = 3) },
        { copy(count = 4) }
    )
}
```

The state list must match exactly the states that are emitted. Each lambda
receives the previous state as the receiver to easily accumulate state changes.

### Asserting posted side effects

```kotlin
testSubject.assert(State()) {
    postedSideEffects(
        Toast(1),
        Toast(2),
        Toast(3),
        Toast(4)
    )
}
```

The side effect list must match exactly the side effects that are emitted.

### Asserting loopbacks

Loopbacks can be tested using a mocking framework like `Mockito` which will
allow you to spy on your `ContainerHost`. It is not the responsibility of this
library to provide this functionality.

```kotlin
val testSubject = spy(SomeClass())

verify(testSubject).doSomething()
verify(testSubject).doSomethingElse(2)
```

### Putting it all together

Since all of the assertions need to be done within the same `assert` block
here's what it looks like once we put it together.

```kotlin
val testSubject = spy(ExampleViewModel()).test(State())

testSubject.testIntent { countToFour() }

testSubject.assert(State()) {
    states(
        { copy(count = 1) },
        { copy(count = 2) },
        { copy(count = 3) },
        { copy(count = 4) }
    )

    postedSideEffects(
        Toast(1),
        Toast(2),
        Toast(3),
        Toast(4)
    )
}

verify(testSubject).doSomething()
verify(testSubject).doSomethingElse(2)
```
## Testing Flows

We can run into situations where we subscribe our [ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
to an infinite (hot) flow of data like so:

```kotlin
val container = container<SomeState, Unit> {
    listenToLocationUpdates()
}

private fun listenToLocationUpdates() = intent {
    runOnSubscription {
        locationService.locationUpdates.collect {
            reduce { state.copy(lng = it.lng, lat = it.lat) }
        }
    }
}
```

We have two options to test code like this.

### Flows in suspending test mode

In this mode an infinite flow would hang our test, since the collect lambda
would never complete. To get around this, we need to provide a fake/mock
finite (cold) flow (e.g. using `flowOf(...)`)

```kotlin
// Fake returning a cold, finite flow. Alternatively use Mockito.
val fakeService = FakeService()
val testSubject = ExampleViewModel(fakeService).test()

testSubject.runOnCreate()

testSubject.assert(State()) {
    states(
        { copy(lng = 1, lat = 1) },
        { copy(lng = 2, lat = 2) },
        { copy(lng = 3, lat = 3) },
    )
}
```

### Flows in live test mode

Flows in this mode don't need to be cold, finite flows. They can remain hot. 
The test won't hang if the
[ContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-container-host/)
connects to such flow, since we're running a real container underneath.

If we're dealing with a producer-style source of infinite values that we can't
control, or some sort of infinite loop it can help to override the dispatchers
with something we can control the scheduling of. For example:

```kotlin
private inner class InfiniteFlowMiddleware : ContainerHost<List<Int>, Nothing> {
    override val container: Container<List<Int>, Nothing> = scope.container(listOf(42))

    fun incrementForever() = intent {
        while (true) {
            delay(30000)
            reduce { state + (state.last() + 1) }
        }
    }
}

@Test
fun `infinite flow test`() = runTest {
        val dispatcher = UnconfinedTestDispatcher()
        val middleware = InfiniteFlowMiddleware().liveTest {
            this.dispatcher = dispatcher
        }

        middleware.testIntent {
            incrementForever()
        }

        dispatcher.scheduler.advanceTimeBy(100000)

        middleware.assert(listOf(42)) {
            states(
                { listOf(42, 43) },
                { listOf(42, 43, 44) },
                { listOf(42, 43, 44, 45) }
            )
        }
    }
```
