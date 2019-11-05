# Orbits

The most important and interesting part of the middleware is the declaration of
orbits. This is what binds actions to transformations and reducers, acting as
the glue between small, distinct functions.

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
    .transform { this.compose(getRandomNumberUseCase) }
    .withReducer { state.copy(currentState.total + event.number) }
```

We can break an orbit into its constituent parts to be able to understand it
better. A typical orbit is made of three sections:

1. Action filter
1. Transformer(s) (optional)
1. React to events (optional): reduce or loopback

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

## Transformers

``` kotlin
.transform { this.compose(getRandomNumberUseCase) }
```

Next we apply transformations to the action observable within the lambda here.
Typically we invoke use cases here. Use cases transform the source action
observable into an observable of a different type of events.

Transformers can be chained to be able to break logic down into smaller pieces
and enable reuse.

## Side effects

We cannot run away from the fact that working with Android will
inherently have some side effects. We've made side effects a first class
citizen in Orbit as we believe that it's better to have a full, clear
view of what side effects are possible in a particular view model.

This functionality is commonly used for things like truly one-off events,
navigation, logging, analytics etc.

It comes in two flavors:

1. `sideEffect` lets us perform side effects that are not intended for
   consumption outside the Orbit container.
1. `postSideEffect` sends the value returned from the closure to a relay
   that can be subscribed when connecting to the view model. Use this for
   view-related side effects like Toasts, Navigation, etc.

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
        .transform { this.compose(getRandomNumberUseCase) }
        .sideEffect { Timber.log(event) }

    perform("post side effect after transformation")
        .on<YetAnotherAction>()
        .transform { this.compose(getRandomNumberUseCase) }
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

## Terminating a chain

Next we have to declare how we will treat the emissions from the transformed
observable. There are two possible reactions:

### Reducers

``` kotlin
.withReducer {
    state.copy(currentState.total + event.number)
}
```

We can apply the `withReducer` function to a transformed observable in order to
reduce its events and the current state to produce a new state.

Reducers can also be applied directly to an action observable, without any
transformations beforehand:

``` kotlin
perform("addition")
    .on<AddAction>()
    .withReducer { state.copy(currentState.total + event.number) }
```

### Loopbacks

``` kotlin
perform("add random number")
    .on<AddAction>()
    .transform { this.compose(getRandomNumberUseCase) }
    .loopBack { event }

perform("reduce add random number")
    .on<GetRandomNumberUseCaseEvent>()
    .withReducer { state.copy(currentState.total + event.number) }
```

Loopbacks allow you to create feedback loops where events coming from one orbit
can create new actions that feed into the system. These are useful to represent
a cascade of events.

### Unterminated chains

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
    .sideEffect { Timber.d(it.toString()) }
```

Unterminated chains still work as you would expect. They will not get looped
back or produce a new state at the end, but they can be useful for e.g. 
analytics.

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

This property always captures the current state, and so calling this 
multiple times within the same DSL block could result in receiving different
values each time as the state gets updated externally.

The only place where we can consider the current state to be non-volatile
is within a reducer.