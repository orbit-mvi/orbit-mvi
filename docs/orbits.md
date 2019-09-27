# Orbits

The most important and interesting part of the middleware is the declaration of
orbits. This is what binds actions to transformations and reducers, acting as
the glue between small, distinct functions.

``` kotlin
perform("add random number")
    .on<AddRandomNumberButtonPressed>()
    .transform { this.compose(getRandomNumberUseCase) }
    .withReducer { state, useCaseEvent ->
        state.copy(state.total + useCaseEvent.number)
    }
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
    .withReducer { state, useCaseEvent ->
        state.copy(state.total + useCaseEvent.number)
    }
```

We can apply the `withReducer` function to a transformed observable in order to
reduce its events and the current state to produce a new state.

Reducers can also be applied directly to an action observable, without any
transformations beforehand:

``` kotlin
orbits {
    perform("addition")
        .on<AddAction>()
        .withReducer { state, action ->
            state.copy(state.total + action.number)
        }
}
```

### Ignored events

``` kotlin
orbits {
    perform("add random number")
        .on<AddRandomNumberButtonPressed>()
        .transform { this.doOnNext(…) }
        .ignoringEvents()
}
```

If we have an orbit that mainly invokes side effects in response to an action
and does not need to produce a new state, we can ignore it.

### Loopbacks

``` kotlin
orbits {
    perform("add random number")
        .on<AddAction>()
        .transform { this.compose(getRandomNumberUseCase) }
        .loopBack { it }

    perform("reduce add random number")
        .on<GetRandomNumberUseCaseEvent>()
        .withReducer { state, event ->
            state.copy(state.total + event.number)
        }
}
```

Loopbacks allow you to create feedback loops where events coming from one orbit
can create new actions that feed into the system. These are useful to represent
a cascade of events.
