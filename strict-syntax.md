# Strict syntax

``` kotlin
class MyViewModel: ContainerHost<MyState, MySideEffect>, ViewModel() {

    override var container = container<MyState, MySideEffect>(MyState())

    fun loadDataForId(id: Int) = orbit {
        sideEffect { post(MySideEffect.Toast("Loading data for $id!")) }
            .transformSuspend { repository.loadData(id) }
            .reduce {
                state.copy(data = result)
            }
    }
}
```

The *classic* Orbit syntax is based on streams. It's close to how MVI is
often portrayed - a reactive cycle. This syntax is great if you're in a
legacy code base with lots of RxJava. Especially if you're thinking of
migrating to coroutines, since you can mix and match both. However it's
not as flexible as the simple syntax due to being stream-based. It's
strictness can be an advantage in larger teams.

Pros:

- Relatively simple
- Hard to make mistakes
- Familiar if you use streams a lot
- Great for code-bases where RxJava and coroutines are mixed

Cons:

- Control-flow logic (e.g. reducing conditionally) is awkward to create
- Not as readable or flexible as the simple syntax
- Interoperability with e.g. RxJava achieved through extra Orbit modules
