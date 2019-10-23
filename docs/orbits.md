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
1. React to events: Ignore, reduce or loopback

## Action filter

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
```

Every orbit must begin with an action filter. Here we declare a orbit
description using the `perform` keyword. The description passed in will appear
in debugging logs if debugging is turned on (WIP).

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

## Reactions

Next we have to declare how we will treat the emissions from the transformed
observable. There are three possible reactions:

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

### Ignored events

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
    .transform { this.map{ … } }
    .ignoringEvents()
```

If we have an orbit that mainly invokes side effects in response to an action
and does not need to produce a new state, we can ignore it.

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

### Side effects

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
        .sideEffect { state, event ->
            Timber.log(inputState)
            Timber.log(action)
        }
        .ignoringEvents()

    perform("side effect after transformation")
        .on<OtherAction>()
        .transform { this.compose(getRandomNumberUseCase) }
        .sideEffect { Timber.log(event) }
        .ignoringEvents()

    perform("add random number")
        .on<YetAnotherAction>()
        .transform { this.compose(getRandomNumberUseCase) }
        .postSideEffect { SideEffect.Toast(event.toString()) }
        .postSideEffect { SideEffect.Navigate(Screen.Home) }
        .ignoringEvents()

    perform("post side effect straight on the incoming action")
        .on<NthAction>()
        .postSideEffect { SideEffect.Toast(inputState.toString()) }
        .postSideEffect { SideEffect.Toast(action.toString()) }
        .postSideEffect { SideEffect.Navigate(Screen.Home) }
        .ignoringEvents()
})
```

The `OrbitContainer` hosted in the `OrbitViewModel` provides a relay that
you can subscribe through the `connect` method on `OrbitViewModel` in order
to receive view-related side effects.
