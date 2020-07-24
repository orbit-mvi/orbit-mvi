# Orbit 2 Coroutines plugin

The coroutine plugin provides:

- Coroutine DSL operators

## Including the module

```kotlin
implementation("com.babylon.orbit2:orbit-coroutines:<latest-version>")
```

## Coroutine DSL Operators

The Core DSL contains the following operators:

- transformSuspend
- transformFlow

### TransformSuspend

``` kotlin
class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example(number: Int) = orbit {
            transformSuspend { delay(100) }
        }
    }
    fun anotherExample() = orbit {
            transformSuspend { apiCall() }
                .transformSuspend { anotherApiCall(event) } // Use the result of the first api call
        }
    }
}
```

Suspending transformers allow you to call suspending functions

### TransformFlow

``` kotlin
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
will emit until completion or until the container has been closed.
