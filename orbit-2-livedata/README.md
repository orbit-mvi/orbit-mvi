# Orbit 2 Live Data plugin

The Live Data plugin provides:

- [Container](../orbit-2-core/src/main/java/com/babylon/orbit2/Container.kt)
  extensions for LiveData streams.

## Including the module

```kotlin
implementation("com.babylon.orbit2:orbit-livedata:<latest-version>") // <-- This module is mandatory
```

## LiveData streams

The Core DSL contains the following Container extension properties:

- stateLiveData
- sideEffectLiveData

This is the recommended way to subscribe to a Container in Android.

Usage:

``` kotlin
class ExampleActivity: Activity() {

    // Example of injection using koin, your DI system might differ
    private val viewModel by viewModel<ExampleViewModel>()

    override fun onCreate() {
        ...

        viewModel.stateLiveData.observe(this) { render(it) }
        viewModel.sideEffectLiveData.observe(this) { handleSideEffect(it) }
    }

    private fun render(state: State) {
        ...
    }

    private fun handleSideEffect(sideEffect: ExampleSideEffect) {
        ...
    }
}

```
