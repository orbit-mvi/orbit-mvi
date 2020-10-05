# Orbit 2 LiveData plugin

- [Orbit 2 LiveData plugin](#orbit-2-livedata-plugin)
  - [transformLiveData](#transformlivedata)

The LiveData plugin provides
[LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
operators.

```kotlin
implementation("com.babylon.orbit2:orbit-livedata:<latest-version>")
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
