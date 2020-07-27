# Orbit 2 Coroutines plugin

- [Orbit 2 Coroutines plugin](#orbit-2-coroutines-plugin)
  - [transformSuspend](#transformsuspend)
  - [transformFlow](#transformflow)
  
The coroutine plugin provides Coroutine Orbit operators.

```kotlin
implementation("com.babylon.orbit2:orbit-coroutines:<latest-version>")
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
[Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt) has
been closed.
