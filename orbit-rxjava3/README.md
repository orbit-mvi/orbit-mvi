# Orbit RxJava3 plugin

- [Orbit RxJava3 plugin](#orbit-rxjava3-plugin)
  - [transformRx3Observable](#transformrx3observable)
  - [transformRx3Single](#transformrx3single)
  - [transformRx3Maybe](#transformrx3maybe)
  - [transformRx3Completable](#transformrx3completable)

The RxJava3 plugin provides RxJava 3 operators.

```kotlin
implementation("org.orbit-mvi.orbit:orbit-rxjava3:<latest-version>")
```

## transformRx3Observable

``` kotlin
fun subscribeToLocationUpdates(): Observable<LocationUpdate> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformRx3Observable { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to a hot or cold [Observable](http://reactivex.io/documentation/observable.html).
This operator acts similar to [flatMap](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmap).

## transformRx3Single

``` kotlin
fun apiCall(): Single<SomeResult> { ... }
fun anotherApiCall(param: SomeResult): Single<OtherResult> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx3Single { apiCall() }
                .transformRx3Single { anotherApiCall(event) } // "event" is the result of the first api call
        }
    }
}
```

You can use this operator to subscribe to an RxJava 3 [Single](http://reactivex.io/documentation/single.html).
This operator acts similar to [flatMapSingle](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapsingle).

## transformRx3Maybe

``` kotlin
fun getLoggedInUser(): Maybe<User> { ... }
fun anotherApiCall(param: User): Single<OtherResult> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx3Maybe { getLoggedInUser() }
                .transformRx3Single { anotherApiCall(event) } // Runs the API call if the user is logged in
        }
    }
}
```

You can use this operator to subscribe to an RxJava 3 `Maybe`.
This operator acts similar to [flatMapMaybe](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapmaybe).

## transformRx3Completable

``` kotlin
fun doSomeWork(): Completable { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx3Completable { doSomeWork() }
        }
    }
}
```

You can use this operator to subscribe to an RxJava 3 `Completable`.
This operator acts similar to [flatMapCompletable](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapcompletable).
