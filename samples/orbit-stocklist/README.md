# Orbit Sample - Stock List

This sample implements a stock list using [Orbit Multiplatform](https://github.com/orbit-mvi/orbit-mvi).

- The application uses Koin for dependency injection which is initialised in
  [StockListApplication](src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/StockListApplication.kt).

- Streaming data is provided by [Lightstreamer](https://lightstreamer.com) and
  their demo server with callback interfaces converted to Kotlin Flow's with
  [callbackFlow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html).

- Navigation between the stock list and the detail view uses Jetpack's [Navigation](https://developer.android.com/guide/navigation)
  and [Safe Args](https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args).
  [ListViewModel](src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/list/business/ListViewModel.kt)
  posts a side effect which [ListFragment](src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/list/ui/ListFragment.kt)
  observes and sends to the `NavController`.

- Both [ListViewModel](src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/list/business/ListViewModel.kt)
  and [DetailViewModel](src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/detail/business/DetailViewModel.kt)
  use `repeatOnSubscription` so streaming data is only collected when the UI is
  active.

- [Data Binding Library](https://developer.android.com/topic/libraries/data-binding)
  is used to populate layouts throughout.
