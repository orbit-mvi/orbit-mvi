# Orbits

The most important and interesting part of the middleware is the declaration of
orbits. This is what binds actions to transformations and reducers, acting as
the glue between small, distinct functions.

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
    .transform { eventObservable.compose(getRandomNumberUseCase) }
    .reduce { currentState.copy(currentState.total + event.number) }
```

We can break an orbit into its constituent parts to be able to understand it
better. A typical orbit is made of:

1. Action filter (required)
1. Transformer(s) (optional)
1. Reducers (optional)
1. Loopbacks (optional)
1. Side effects (optional)

## Action filter

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
```

Every orbit must begin with an action filter. Here we declare a orbit
description using the `perform` keyword. The description passed in will appear
in debugging logs if debugging is turned on (WIP).

Unique names must be used for each `perform` within a middleware or view model.

Then we declare an action that this orbit will react to using the `on` keyword.
We can also declare a list of actions if this orbit reacts to a few different
actions.

The type provided to `on` is out-projected so subclasses of the given type
will also trigger this flow. For example, an `on<Number>()` will be triggered
for `Int`, `Float`, etc.

**NOTE**
Be careful not to use generic types in these filters! Due to type erasure
e.g. `List<Int>` and `List<String>` resolve to the same class, potentially
causing unintended events or crashes.

## Transformers

``` kotlin
.transform { eventObservable.compose(getRandomNumberUseCase) }
```

Next we apply transformations to the action observable within the lambda here.
Typically we invoke use cases here. Use cases transform the source action
observable into an observable of a different type of events.

Transformers can be chained to be able to break logic down into smaller pieces
and enable reuse.

## Reducers

``` kotlin
.reduce {
    state.copy(currentState.total + event.number)
}
```

We can apply the `reduce` function to a transformed observable in order to
reduce its events and the current state to produce a new state.

Reducers can also be applied directly to an action observable, without any
transformations beforehand:

``` kotlin
perform("addition")
    .on<AddAction>()
    .reduce { state.copy(currentState.total + event.number) }
```

Reducers are passthrough transformers. This means that after applying
a reducer, the upstream events are passed through unmodified.

However, operators downstream of a reducer can expect to be called only
after the upstream reduction has completed, so a call to `currentState`
will yield the recently reduced state.

## Loopbacks

``` kotlin
perform("add random number")
    .on<AddAction>()
    .transform { eventObservable.compose(getRandomNumberUseCase) }
    .loopBack { event }

perform("reduce add random number")
    .on<GetRandomNumberUseCaseStatus>()
    .reduce { state.copy(currentState.total + event.number) }
```

Loopbacks allow you to create feedback loops where events coming from one orbit
can create new actions that feed into the system. These are useful to represent
a cascade of events.

Loopbacks are passthrough transformers. This means that after applying
a loopback, the upstream events are passed through unmodified.

## Side effects

We cannot run away from the fact that working with Android will
inherently have some side effects. We've made side effects a first class
citizen in Orbit as we believe that it's better to have a full, clear
view of what side effects are possible in a particular view model.

This functionality is commonly used for things like truly one-off events,
navigation, logging, analytics etc.

You can use the `post` method within `sideEffect` in order to
send the value to a relay that can be subscribed to when connecting to the
view model. Use this for view-related side effects like Toasts, Navigation,
etc.

``` kotlin
sealed class SideEffect {
    data class Toast(val text: String) : SideEffect()
    data class Navigate(val screen: Screen) : SideEffect()
}

OrbitViewModel<State, SideEffect>(State(), {

    perform("side effect straight on the incoming action")
        .on<SomeAction>()
        .sideEffect {
            Timber.log(currentState)
            Timber.log(event)
        }

    perform("side effect after transformation")
        .on<OtherAction>()
        .transform { eventObservable.compose(getRandomNumberUseCase) }
        .sideEffect { Timber.log(event) }

    perform("post side effect after transformation")
        .on<YetAnotherAction>()
        .transform { eventObservable.compose(getRandomNumberUseCase) }
        .sideEffect { post(SideEffect.Toast(event.toString())) }
        .sideEffect { post(SideEffect.Navigate(Screen.Home)) }

    perform("post side effect straight on the incoming action")
        .on<NthAction>()
        .sideEffect { post(SideEffect.Toast(currentState.toString())) }
        .sideEffect { post(SideEffect.Toast(event.toString())) }
        .sideEffect { post(SideEffect.Navigate(Screen.Home)) }
})
```

The `OrbitContainer` hosted in the `OrbitViewModel` provides a relay that
you can subscribe through the `connect` method on `OrbitViewModel` in order
to receive view-related side effects.

The side effects are passthrough transformers. This means that after applying
a side effect, the upstream events are passed through unmodified.

## Accessing the current state

It's fairly common to read the current state in order to perform some
operation in your transformer, or side effect. You can capture the current
state at any point within each DSL block by simply calling `currentState`

For example:

``` kotlin
perform("Toast the current state")
    .on<SomeAction>()
    .sideEffect { post(SideEffect.Toast(currentState.toString())) }
```

This property always reads the current state from the orbit container,
so calling this multiple times within the same DSL block could result
in receiving different values each time as the state gets updated
externally.

The only place where we can consider the current state to be non-volatile
is within a reducer.

## Flexible flows

You can chain as many operators as you want along the way. Remember that
the three passthrough transformer functions are:

1. reducers
1. side effects
1. loopbacks

This means they do not modify the upstream, allowing you to build flexible
chains of logic:

``` kotlin
perform("load patient prescriptions")
    .on<LifecycleAction.Created>()
    .transform { eventObservable.compose(getCurrentPatientUseCase) }
    .reduce {
        when(event) {
            is Status.Result -> currentState.copy(
                data = event.data,
                loading = false
            )
            is Status.Loading -> currentState.copy(loading = true)
            is Status.Error -> currentState.copy(
                error = event.throwable,
                loading = true
            )
        }
     }
    .transform {
        // Run only if previous use case was successful
        eventObservable
            .filter { event.data != null }
            .compose(getPatientPrescriptionsUseCase)
    }
    .reduce { ... }
```

The important thing to note is to not overdo it. Finish a chain with a
loopback if you think it's getting too complicated.

Or don't do this at all if that's your preference! You can always assume a
rule to always finish on the first reducer / loopback to keep things simple.

Orbit gives you the flexibility to express your logic however you think is
sensible.

## Lifecycle actions

For convenience we automatically send lifecycle actions into the MVI system.

Currently the only lifecycle action available is `LifecycleAction.Created`
which is sent to the MVI system when a `BaseOrbitContainer` is instantiated.

``` kotlin
perform("load initial data")
    .on<LifecycleAction.Created>()
    .transform { eventObservable.compose(getSomeDataUseCase) }
    .reduce { ... }
```

This is useful to e.g. start loading initial screen data as soon as the
`OrbitViewModel` is created.

Currently we do not provide any more lifecycle actions but we are considering
extending that list to provide automatic lifecycle events from Android.
