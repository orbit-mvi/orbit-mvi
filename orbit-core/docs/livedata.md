# LiveData compatibility

To use LiveData with Orbit simply use the Kotlin coroutines extensions by
including one of the following dependencies:

```kotlin
dependencies {
    // For LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:<latest-version>")
}
```

Then simply use [asFlow](https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#asflow)
to convert your LiveData objects into coroutines.

```kotlin
interface Repository {
    fun loadAsLiveData(): LiveData<Data>
}

class MyViewModel(
    repository: Repository
): ContainerHost<MyState, MySideEffect>, ViewModel() {

    override val container = container<MyState, MySideEffect>(MyState())

    // If your stream is infinite ensure you disable idling resource handling
    // if you use Espresso
    fun stream() = intent(idlingResource = false) {
        val result = repository.loadAsLiveData().asFlow().collect {
            reduce {
                state.copy(data = result)
            }
        }
    }

    fun single() = intent {
        // If the LiveData is only going to return a single value use first()
        val result = repository.loadAsLiveData().asFlow().first()

        reduce {
            state.copy(data = result)
        }
    }
}
```

We strongly recommend you observe your ViewModel state stream using the
built-in Flow, however, if you wish you can convert this into a LiveData object
with [asLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/package-summary#aslivedata).
