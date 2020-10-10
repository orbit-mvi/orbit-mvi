# Orbit 2 Unit Testing module

The module provides a simple unit testing framework for your Orbit 2
[ContainerHosts](../orbit-2-core/src/main/java/com/babylon/orbit2/ContainerHost.kt).

## Including the module

```kotlin
testImplementation("com.babylon.orbit2:orbit-test:<latest-version>")
```

## Testing goals

Orbit is a well structured framework and as such, the tests will wrap around
the framework's structure.

Experience with Orbit 1 has taught us what works and what doesn't. This helped
us put constraints around our tests that we hope will make your tests
predictable and easy to write and maintain.

Things that we consider important to test:

- Emitted states
- Emitted side effects
- Loopbacks i.e. flow A calling flow B from a `sideEffect`
- Potentially dependencies being called

The last item on the list is outside of the scope of this library and can be
easily tested using a mocking framework like `mockito`.

For the first three things we have created utilities that should make them easy
to test.

### Additional constraints

There are a couple of additional constraints that we put on our tests to make
them more predictable.

- Run the
  [Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt) in
  blocking mode
- Isolate the first function called on the
  [ContainerHost](../orbit-2-core/src/main/java/com/babylon/orbit2/ContainerHost.kt)

Isolating flows helps avoid unexpected state/side effect emissions from
loopbacks in your flow under test. This can be turned off if you have a
particular testing need.

## Testing method

First we need to put our
[ContainerHost](../orbit-2-core/src/main/java/com/babylon/orbit2/ContainerHost.kt)
into test mode and call our flow (method) under test. Let's assume we've made a
`ViewModel` the host.

```kotlin
data class State(val count: Int = 0)

val testSubject = ExampleViewModel().test(State())

testSubject.countToFour()
```

### Asserting states

Having done the above, we can move to assertions. We do not need to assert for
the initial state, this is done automatically as a sanity check.

```kotlin
testSubject.assert {
    states(
        { Toast(1) },
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
testSubject.assert {
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

```kotlin
testSubject.assert {
    loopBack { doSomething() }
    loopBack { doSomethingElse(2) }
}
```

Each loopback is asserted individually.

### Putting it all together

Since all of the assertions need to be done within the same `assert` block
here's what it looks like once we put it together.

```kotlin
val testSubject = ExampleViewModel().test(State())

testSubject.countToFour()

testSubject.assert {
    states(
        { Toast(1) },
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

    loopBack { doSomething() }
    loopBack { doSomethingElse(2) }
}
```
