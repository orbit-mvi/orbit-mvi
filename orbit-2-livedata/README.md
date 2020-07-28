# Orbit 2 LiveData plugin

The LiveData plugin provides the following
[Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt)
extension properties:

- [stateLiveData](src/main/java/com/babylon/orbit2/LiveDataPlugin.kt#stateLiveData)
- [sideEffectLiveData](src/main/java/com/babylon/orbit2/LiveDataPlugin.kt#sideEffectLiveData)

## Including the module

```kotlin
implementation("com.babylon.orbit2:orbit-livedata:<latest-version>")
```

## Usage

Below is the recommended way to subscribe to a
[Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt) in
Android.

``` kotlin
class ExampleActivity: AppCompatActivity() {

    // Example of injection using koin, your DI system might differ
    private val viewModel by viewModel<ExampleViewModel>()

    override fun onCreate(savedState: Bundle?) {
        ...

        viewModel.container.stateLiveData.observe(this, Observer {render(it) })
        viewModel.container.sideEffectLiveData.observe(this, Observer {handleSideEffect(it) })
    }

    private fun render(state: CalculatorState) {
        ...
    }

    private fun handleSideEffect(sideEffect: ExampleSideEffect) {
        ...
    }
}

```
