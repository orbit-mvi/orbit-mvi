# Orbit 2 RxJava1 plugin

- [Orbit 2 RxJava1 plugin](#orbit-2-rxjava1-plugin)
  - [transformRx1Observable](#transformrx1observable)
  - [transformRx1Single](#transformrx1single)
  - [transformRx1Completable](#transformrx1completable)

The RxJava1 plugin provides RxJava 1 operators.

```kotlin
implementation("com.babylon.orbit2:orbit-rxjava1:<latest-version>")
```

## transformRx1Observable

``` kotlin
fun subscribeToLocationUpdates(): Observable<LocationUpdate> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformRx1Observable { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to a hot or cold [Observable](http://reactivex.io/documentation/observable.html).
This operator acts similar to [flatMap](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmap).

## transformRx1Single

``` kotlin
fun apiCall(): Single<SomeResult> { ... }
fun anotherApiCall(param: SomeResult): Single<OtherResult> { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx1Single { apiCall() }
                .transformRx1Single { anotherApiCall(event) } // "event" is the result of the first api call
        }
    }
}
```

You can use this operator to subscribe to an RxJava 1 [Single](http://reactivex.io/documentation/single.html).
This operator acts similar to [flatMapSingle](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapsingle).

## transformRx1Completable

``` kotlin
fun doSomeWork(): Completable { ... }

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun example() = orbit {
            transformRx1Completable { doSomeWork() }
        }
    }
}
```

You can use this operator to subscribe to an RxJava 1 `Completable`.
This operator acts similar to [flatMapCompletable](https://github.com/ReactiveX/RxJava/wiki/Transforming-Observables#flatmapcompletable).
