# Orbit Coroutines plugin

- [Orbit Coroutines plugin](#orbit-coroutines-plugin)
  - [transformSuspend](#transformsuspend)
  - [transformFlow](#transformflow)
  
The coroutine plugin provides Coroutine Orbit operators.

```kotlin
implementation("org.orbit-mvi.orbit:orbit-coroutines:<latest-version>")
```

## transformSuspend

``` kotlin
suspend fun apiCall(): SomeResult { ... }
suspend fun anotherApiCall(param: SomeResult): OtherResult { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example(number: Int) = orbit {
            transformSuspend { delay(100) }
        }
    }
    fun anotherExample() = orbit {
            transformSuspend { apiCall() }
                .transformSuspend { anotherApiCall(event) } // "event" is the result of the first api call
        }
    }
}
```

Suspending transformers allow you to call suspending functions

## transformFlow

``` kotlin
fun subscribeToLocationUpdates(): Flow<LocationUpdate> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformFlow { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to hot or cold coroutine flows. The flows
will emit until completion or until the
[Container](../orbit-core/src/main/kotlin/org/orbitmvi/orbit/Container.kt) has
been closed.
