# Orbit LiveData plugin

- [Orbit LiveData plugin](#orbit-livedata-plugin)
  - [transformLiveData](#transformlivedata)

The LiveData plugin provides
[LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
operators.

```kotlin
implementation("org.orbitmvi.orbit:orbit-livedata:<latest-version>")
```

## transformLiveData

``` kotlin
fun subscribeToLocationUpdates(): LiveData<LocationUpdate>()

class ExampleViewModel : ContainerHost<ExampleState, ExampleSideEffect> {
    ...

    fun startLocationTracking() = orbit {
            transformLiveData { subscribeToLocationUpdates() }
                .reduce { state.copy(currentLocation = event) }
        }
    }
}
```

You can use this operator to subscribe to a [LiveData](https://developer.android.com/topic/libraries/architecture/livedata).
