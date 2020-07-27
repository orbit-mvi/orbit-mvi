# Orbit 2 RxJava2 plugin

- [Orbit 2 RxJava2 plugin](#orbit-2-rxjava2-plugin)
  - [transformObservable](#transformobservable)
  - [transformSingle](#transformsingle)
  - [transformMaybe](#transformmaybe)
  - [transformCompletable](#transformcompletable)

The RxJava plugin provides RxJava 2 operators.

```kotlin
implementation("com.babylon.orbit2:orbit-rxjava2:<latest-version>")
```

## transformObservable

``` kotlin
fun subscribeToLocationUpdates(): Observable<LocationUpdate> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformObservable { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to a hot or cold [Observable](http://reactivex.io/documentation/observable.html).
This operator acts similar to [flatMap](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmap).

## transformSingle

``` kotlin
fun apiCall(): Single<SomeResult> { ... }
fun anotherApiCall(param: SomeResult): Single<OtherResult> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformSingle { apiCall() }
                .transformSingle { anotherApiCall(event) } // "event" is the result of the first api call
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 [Single](http://reactivex.io/documentation/single.html).
This operator acts similar to [flatMapSingle](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapsingle).

## transformMaybe

``` kotlin
fun getLoggedInUser(): Maybe<User> { ... }
fun anotherApiCall(param: User): Single<OtherResult> { ... }

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
This operator acts similar to [flatMapMaybe](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapmaybe).

## transformCompletable

``` kotlin
fun doSomeWork(): Completable { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformCompletable { doSomeWork() }
        }
    }
}
```

You can use this operator to subscribe to an RxJava 2 `Completable`.
This operator acts similar to [flatMapCompletable](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapcompletable).
