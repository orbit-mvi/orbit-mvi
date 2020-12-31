# Orbit RxJava2 plugin

- [Orbit RxJava2 plugin](#orbit-rxjava2-plugin)
  - [transformRx2Observable](#transformrx2observable)
  - [transformRx2Single](#transformrx2single)
  - [transformRx2Maybe](#transformrx2maybe)
  - [transformRx2Completable](#transformrx2completable)

The RxJava2 plugin provides RxJava 2 operators.

```kotlin
implementation("org.orbit-mvi.orbit:orbit-rxjava2:<latest-version>")
```

## transformRx2Observable

``` kotlin
fun subscribeToLocationUpdates(): Observable<LocationUpdate> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformRx2Observable { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to a hot or cold [Observable](http://reactivex.io/documentation/observable.html).
This operator acts similar to [flatMap](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmap).

## transformRx2Single

``` kotlin
fun apiCall(): Single<SomeResult> { ... }
fun anotherApiCall(param: SomeResult): Single<OtherResult> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx2Single { apiCall() }
                .transformRx2Single { anotherApiCall(event) } // "event" is the result of the first api call
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 [Single](http://reactivex.io/documentation/single.html).
This operator acts similar to [flatMapSingle](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapsingle).

## transformRx2Maybe

``` kotlin
fun getLoggedInUser(): Maybe<User> { ... }
fun anotherApiCall(param: User): Single<OtherResult> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx2Maybe { getLoggedInUser() }
                .transformRx2Single { anotherApiCall(event) } // Runs the API call if the user is logged in
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 `Maybe`.
This operator acts similar to [flatMapMaybe](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapmaybe).

## transformRx2Completable

``` kotlin
fun doSomeWork(): Completable { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx2Completable { doSomeWork() }
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 `Completable`.
This operator acts similar to [flatMapCompletable](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapcompletable).
