---
sidebar_label: Combining hosts
---

# Combining hosts

The `combine` family lets you derive a single
[OrbitContainerHost](pathname:///dokka/orbit-core/org.orbitmvi.orbit/-orbit-container-host/)
from the external states (and, optionally, side effects) of up to five upstream
hosts. The combined host exposes a derived external state computed via a
`transformState` lambda, mirroring the behaviour of
[kotlinx.coroutines.flow.combine](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/combine.html).

This is useful when a screen depends on several independent hosts that each own
a slice of business logic, and you want to present them to the UI as one state.

:::caution

The `combine` API is annotated `@OrbitExperimental` and may change. Opt in with
`@OptIn(OrbitExperimental::class)` to use it.

:::

## Read-only combine

The top-level `combine` forms produce a read-only host: the combined host has no
state of its own, so its internal state is `Unit` and calling `intent` (or
`orbit`/`inlineOrbit`) throws. You provide the
[CoroutineScope](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/)
that hosts the combined container.

```kotlin
class UserHost(scope: CoroutineScope) : OrbitContainerHost<User, User, Nothing> {
    override val container = scope.orbitContainer<User, Nothing>(User.Empty)
}

class CartHost(scope: CoroutineScope) : OrbitContainerHost<Cart, Cart, Nothing> {
    override val container = scope.orbitContainer<Cart, Nothing>(Cart.Empty)
}

// Read-only combined host scoped to `scope`
val userCart = combine(scope, userHost, cartHost) { user, cart ->
    UserCart(user, cart)
}

userCart.container.externalStateFlow.collect { (user, cart) -> render(user, cart) }
```

The combined `externalStateFlow` re-emits whenever any upstream host's external
state changes, serving only distinct values.

## Master host (receiver form)

When you call `combine` as an extension on an existing host, that **receiver**
becomes the "master" of the combined host. The combined host keeps the master's
internal state and intent dispatching, so `intent { reduce { ... } }` on the
combined host mutates the master's own internal state — while the combined host
still exposes the derived external state to the UI.

This lets a parent host keep its own business logic and internal state, yet
present a combined view of itself plus its children.

```kotlin
class CheckoutHost(scope: CoroutineScope) : OrbitContainerHost<CheckoutState, CheckoutState, CheckoutEvent> {
    override val container = scope.orbitContainer<CheckoutState, CheckoutEvent>(CheckoutState())

    fun applyPromo(code: String) = intent {
        reduce { state.copy(promo = code) }
    }
}

class CheckoutContainerHost(
    private val checkoutHost: CheckoutHost,
    private val cartHost: CartHost,
) : OrbitContainerHost<CheckoutState, CheckoutView, CheckoutEvent> {

    // The receiver (checkoutHost) is the master. The combined host's internal
    // state and side-effect type are the master's; the external state is derived.
    override val container = checkoutHost.combine(cartHost) { checkout, cart ->
        CheckoutView(checkout, cart)
    }.container

    // Runs on the master — mutates CheckoutHost's internal state.
    fun applyPromo(code: String) = intent {
        reduce { state.copy(promo = code) }
    }
}
```

For the receiver form:

- The combined host's internal state type and side-effect type are the master's.
- `stateFlow` / `refCountStateFlow` are the master's; `externalStateFlow` is the
  derived combined state.
- `intent`, `orbit`, `inlineOrbit` and `joinIntents` delegate to the master.
- The combined host is scoped to the master's `container.scope`.
- In the no-`transformSideEffects` overload above, the master's own side effects
  flow straight through the combined `sideEffectFlow`; child hosts' side effects
  are **not** forwarded. Use the side-effect overload below to merge them.

## Merging side effects

To combine side effects, supply a `transformSideEffects` lambda. It is shaped as
a `FlowCollector` extension over the upstream side-effect flows, so you `emit`
(or `emitAll`) the combined values — typically with
[merge](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/merge.html):

```kotlin
sealed interface CheckoutEvent {
    data class Auth(val event: AuthEvent) : CheckoutEvent
    data class Payment(val event: PaymentEvent) : CheckoutEvent
}

val checkout = authHost.combine(
    other = paymentHost,
    transformState = { auth, payment -> CheckoutState(auth, payment) },
    transformSideEffects = { authEffects, paymentEffects ->
        emitAll(
            merge(
                authEffects.map(CheckoutEvent::Auth),
                paymentEffects.map(CheckoutEvent::Payment),
            )
        )
    },
)

checkout.container.sideEffectFlow.collect { event -> /* navigate, toast, etc. */ }
```

In the receiver (master) form with a `transformSideEffects` lambda:

- The combined side-effect type is the user-chosen type `T` produced by the
  lambda.
- Side effects posted from intents on the combined host
  (`intent { postSideEffect(...) }`) surface directly on the combined
  `sideEffectFlow`.
- The master's own native side effects reach `transformSideEffects` as the first
  upstream flow, alongside the children.

:::note

`transformSideEffects` must not return early. Once it returns, no further side
effects are emitted for the current subscription window. The idiomatic form is
`emitAll(merge(...))`, which never completes.

:::

Upstream subscription is gated on combined-host subscribers: while nobody is
collecting the combined `sideEffectFlow`, the lambda is suspended and upstream
[repeatOnSubscription](index.md#repeat-on-subscription) blocks correctly observe
the unsubscribed state. The combined side-effect flow is broadcast to all current
collectors with no replay cache.

:::caution

In `SideEffectMode.FAN_OUT` mode the combined host *consumes* upstream side
effects while subscribed, so other direct collectors of those upstream flows
will not receive them. Prefer `SideEffectMode.BROADCAST` (the default) when an
upstream host participates in a `combine`.

:::

## Higher arities and chaining

Both flavours are available at arities 2 to 5:

```kotlin
val combined = a.combine(b, c, d, e) { sa, sb, sc, sd, se -> View(sa, sb, sc, sd, se) }
```

`combine` calls can also be chained. In the receiver form the original master is
preserved through the chain, so intents on the final combined host still reach
it:

```kotlin
val abc = a.combine(b) { x, y -> x + y }
    .combine(c) { sum, z -> sum * z }

// Still dispatches to `a` (the original master)
abc.intent { reduce { state + 1 } }
```

## ViewModel scope

In the [ViewModel module](../ViewModel/index.md), the top-level `combine`
overloads are available as `ViewModel` extensions that default to
`viewModelScope`, so you don't need to pass a scope. The combined container is
cleaned up automatically when the `ViewModel` is cleared.

```kotlin
class CheckoutViewModel(
    private val authHost: AuthHost,
    private val cartHost: CartHost,
    private val paymentHost: PaymentHost,
) : ViewModel(), OrbitContainerHost<Unit, CheckoutState, CheckoutEvent> {

    // Uses viewModelScope automatically
    override val container = combine(
        authHost, cartHost, paymentHost,
        transformState = { auth, cart, payment -> CheckoutState(auth, cart, payment) },
        transformSideEffects = { authFx, cartFx, payFx ->
            emitAll(
                merge(
                    authFx.map(CheckoutEvent::Auth),
                    cartFx.map(CheckoutEvent::Cart),
                    payFx.map(CheckoutEvent::Payment),
                )
            )
        },
    ).container
}
```

These `ViewModel` forms are read-only (there is no master), exactly like the
top-level `combine(scope, ...)` forms.
