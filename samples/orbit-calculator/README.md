# Orbit Sample - Calculator

This sample implements a simple calculator using [Orbit MVI](https://github.com/babylonhealth/orbit-mvi).

- The application uses the [simple syntax](../../simple-syntax.md).

- The application uses Koin for dependency injection which is initialised in
  [CalculatorApplication](src/main/kotlin/org/orbitmvi/orbit/sample/calculator/CalculatorApplication.kt).

- [CalculatorActivity](src/main/kotlin/org/orbitmvi/orbit/sample/calculator/CalculatorActivity.kt)
  uses the [Data Binding Library](https://developer.android.com/topic/libraries/data-binding)
  to provide the [CalculatorViewModel](src/main/kotlin/org/orbitmvi/orbit/sample/calculator/CalculatorViewModel.kt)
  to the layout [activity_main.xml](src/main/res/layout/activity_main.xml). The
  layout accesses the current state through LiveData.

- [CalculatorViewModel](src/main/kotlin/org/orbitmvi/orbit/sample/calculator/CalculatorViewModel.kt)
  uses a `SavedStateHandle` for retaining the current state. It implements a
  private [ContainerHost](../../orbit-core/src/commonMain/kotlin/org/orbitmvi/orbit/ContainerHost.kt)
  so the internal implementation of [CalculatorState](src/main/kotlin/org/orbitmvi/orbit/sample/calculator/CalculatorState.kt)
  is not exposed.

## How the calculator works

The calculator itself is based off the principle of two registers, x and y. In
short:

- Digits are stored in the x register.
- The x register is rendered to screen unless it is empty in which case we
  render the y register.
- Plus, minus, multiply and divide operators transfer data from x to y register
  and then clears x. The operator is stored as it is not actioned immediately.
- Equals operator performs the calculation using the stored operator and the
  values in the x and y registers.
