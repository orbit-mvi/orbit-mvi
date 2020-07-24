# Orbit 2 RxJava2 plugin

The RxJava 2 plugin provides:

- RxJava 2 DSL operators

## Including the module

```kotlin
implementation("com.babylon.orbit2:orbit-rxjava2:<latest-version>") // <-- This module is mandatory
```

## RxJava 2 DSL Operators

The Core DSL contains the following operators:

- transformObservable
- transformSingle
- transformMaybe
- transformCompletable

### transformObservable

``` kotlin
class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformObservable { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to hot or cold observables.
This operator acts similar to `flatMap`.

### transformSingle

``` kotlin
class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformSingle { apiCall() }
                .transformSingle { anotherApiCall(event) } // Use the result of the first api call
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 `Single`.
This operator acts similar to `flatMapSingle`.

### transformMaybe

``` kotlin
class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformMaybe { getLoggedInUser() }
                .transformSingle { anotherApiCall(event) } // Runs the API call if the user is logged in
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 `Maybe`.
This operator acts similar to `flatMapMaybe`.

### transformCompletable

``` kotlin
class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformCompletable { doSomeWork() }
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 `Completable`.
This operator acts similar to `flatMapCompletable`.
