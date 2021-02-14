# Simple syntax

``` kotlin
class MyViewModel: ContainerHost<MyState, MySideEffect>, ViewModel() {

    override val container = container<MyState, MySideEffect>(MyState())

    fun loadDataForId(id: Int) = intent {
        postSideEffect(MySideEffect.Toast("Loading data for $id!"))

        val result = repository.loadData(id)

        reduce {
            state.copy(data = result)
        }
    }
}
```

This syntax integrates with the coroutine framework to bring you something that
you will be very comfortable with if you already use coroutines for your
project. On the other hand, it's slightly easier to make mistakes if you don't
know how to use coroutines effectively. For example, blocking code in
`intent` can block your `Container`.

Pros:

- Extremely light and flexible
- Your MVI logic executes in a suspend function
- Interoperability with e.g. RxJava achieved through standard Kotlin libraries

Cons:

- Your code has to conform to coroutine best practices
