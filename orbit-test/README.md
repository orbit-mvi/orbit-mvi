# Orbit Unit Testing module

The module provides a simple unit testing framework for your Orbit
[ContainerHosts](../orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt).

## Including the module

```kotlin
testImplementation("org.orbit-mvi:orbit-test:<latest-version>")
```

## Testing goals

Orbit is a well structured framework and as such, the tests will wrap around the
framework's structure.

Experience with Orbit 1 has taught us what works and what doesn't. This helped
us put constraints around our tests that we hope will make your tests
predictable and easy to write and maintain.

Things that we consider important to test:

- Emitted states
- Emitted side effects
- Loopbacks i.e. intent A calling intent B
- Potentially dependencies being called

The last two items on the list are outside of the scope of this library and can
be easily tested using a mocking framework like `mockito`.

For the first two things we have created utilities that should make them easy to
test.

### Additional constraints

There are a couple of additional constraints that we can put on our tests to
make them more predictable.

- Run the calls invoked on your
  [Container](../orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/Container.kt)
  as suspending function
- Isolate the first function called on the
  [ContainerHost](../orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)

Isolating intents helps avoid unexpected state/side effect emissions from
loopbacks in your intent under test. This can be turned off if you have a
particular testing need.

## Testing method

First we need to put our
[ContainerHost](../orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)
into test mode and call our intent (method) under test. Let's assume we've made
a `ViewModel` the host.

```kotlin
data class State(val count: Int = 0)

val testSubject = ExampleViewModel().test(State())

testSubject.testIntent { countToFour() }
```

### Run `onCreate`

If the `Container` is created with `CoroutineScope.container()` or
`ViewModel.container()` there is an option to provide the `onCreate` property.
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
