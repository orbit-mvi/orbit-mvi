# Orbit Sample - Compose Stock List

This sample implements a stock list using [Orbit MVI](https://github.com/orbit-mvi/orbit-mvi).

- The application uses Dagger Hilt for dependency injection which is initialised
  in [StockListApplication](app/src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/StockListApplication.kt).

- Streaming data is provided by [Lightstreamer](https://lightstreamer.com) and
  their demo server with callback interfaces converted to Kotlin Flow's with
  [callbackFlow](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html).

- Navigation between the stock list and the detail view uses Jetpack's [Navigation](https://developer.android.com/jetpack/compose/navigation).
  [ListViewModel](app/src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/list/business/ListViewModel.kt)
  posts a side effect which [ListScreen](app/src/main/kotlin/org/orbitmvi/orbit/sample/stocklist/list/ui/ListScreen.kt)
  observes and sends to the `NavController`.

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
  is used to render layouts throughout.
