# Orbit 2 LiveData plugin

- [Orbit 2 LiveData plugin](#orbit-2-livedata-plugin)
  - [Container extensions](#container-extensions)
  - [transformLiveData](#transformlivedata)

The LiveData plugin provides
[LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
extensions and operators.

```kotlin
implementation("com.babylon.orbit2:orbit-livedata:<latest-version>")
```

## Container extensions

The LiveData plugin provides the following
[Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt)
extension properties:

- [state](src/main/java/com/babylon/orbit2/livedata/LiveDataPlugin.kt#state)
- [sideEffect](src/main/java/com/babylon/orbit2/livedata/LiveDataPlugin.kt#sideEffect)

These extensions will be removed in Orbit 1.2.0 due to fundamental
incompatibility of `LiveData` design with Orbit design goals.

``` kotlin
class ExampleActivity: AppCompatActivity() {

    // Example of injection using koin, your DI system might differ
    private val viewModel by viewModel<ExampleViewModel>()

    override fun onCreate(savedState: Bundle?) {
        ...

        viewModel.container.state.observe(this) {render(it) }
        viewModel.container.sideEffect.observe(this) {handleSideEffect(it) }
    }

    private fun render(state: CalculatorState) {
        ...
    }

    private fun handleSideEffect(sideEffect: ExampleSideEffect) {
        ...
    }
}
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
