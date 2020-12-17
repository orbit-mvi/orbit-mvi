# Orbit 2 Sample - Posts

This sample implements a simple master-detail application using
[Orbit MVI](https://github.com/babylonhealth/orbit-mvi).

- The application uses the [simple syntax](../../simple-syntax.md).

- The application uses Koin for dependency injection which is initialised in
  [PostsApplication](src/main/kotlin/com/babylon/orbit2/sample/posts/app//PostsApplication.kt).

- [PostListViewModel](src/main/kotlin/com/babylon/orbit2/sample/posts/app/features/postlist/viewmodel/PostListViewModel.kt)
  loads a list of posts. Upon clicking a post it navigates to the
  [PostDetailsFragment](src/main/kotlin/com/babylon/orbit2/sample/posts/app/features/postdetails/ui/PostDetailsFragment.kt)
  which displays the details of the clicked post.

- Navigation between the list and the detail view uses Jetpack's
  [Navigation](https://developer.android.com/guide/navigation) and
  [SafeArgs](https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args).
  [PostListViewModel](src/main/kotlin/com/babylon/orbit2/sample/posts/app/features/postlist/viewmodel/PostListViewModel.kt)
  posts a side effect which
  [PostListFragment](src/main/kotlin/com/babylon/orbit2/sample/posts/app/features/postlist/ui/PostListFragment.kt)
  observes and sends to the `NavController`.

- The state is accessed in the fragments through `Flow`.

- [PostListViewModel](src/main/kotlin/com/babylon/orbit2/sample/posts/app/features/postlist/viewmodel/PostListViewModel.kt)
  and
  [PostDetailsViewModel](src/main/kotlin/com/babylon/orbit2/sample/posts/app/features/postdetails/viewmodel/PostDetailsViewModel.kt)
  use a `SavedStateHandle` for retaining the current state.
