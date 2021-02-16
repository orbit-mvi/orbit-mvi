# RxJava compatibility

To use RxJava with Orbit simply use the Kotlin coroutines extensions by
including one of the following dependencies:

```kotlin
dependencies {
    // For RxJava 2
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:<latest-version>")

    // For RxJava 3
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3:<latest-version>")
}
```

Then simply use the extensions to convert your RxJava objects into coroutines.
Please note you need to ensure your RxJava objects are already being observed
on a background thread, otherwise add `.observeOn(Dispatchers.IO)` before
`await()`.

```kotlin
interface Repository {
    fun loadDataAsSingle(): Single<Data>
    fun loadDataAsObservable(): Observable<Data>
}

class MyViewModel(
    repository: Repository
): ContainerHost<MyState, MySideEffect>, ViewModel() {

    override val container = container<MyState, MySideEffect>(MyState())

    fun single() = intent {
        // await() applies to Single, Maybe and Completable
        val result = repository.loadDataAsSingle().await()

        reduce {
            state.copy(data = result)
        }
    }

    // If your stream is infinite ensure you disable idling resource handling
    // if you use Espresso
    fun observable() = intent(idlingResource = false) {
        val result = repository.loadDataAsObservable().asFlow().collect {
            reduce {
                state.copy(data = result)
            }
        }
    }
}
```

We strongly recommend you observe your ViewModel state stream using the
built-in Flow, however, if you wish you can convert this into an RxJava object
with [asObservable](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-rx3/kotlinx.coroutines.rx3/kotlinx.coroutines.flow.-flow/as-observable.html).
