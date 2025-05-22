# Orbit Sample - Posts Compose Multiplatform

This sample implements a simple master-detail application using
[Orbit Multiplatform](https://github.com/orbit-mvi/orbit-mvi).

- The application uses Koin for dependency injection which is initialised in
  [App](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/App.kt).

- [PostListViewModel](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/domain/viewmodel/list/PostListViewModel.kt)
  loads a list of posts. Upon clicking a post it navigates to the
  [PostDetailsScreen](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/app/features/postdetails/ui/PostDetailsScreen.kt)
  which displays the details of the clicked post.

- Navigation between the list and the detail view uses Jetpack's
  [Navigation with Compose](https://developer.android.com/develop/ui/compose/navigation).
  [PostListViewModel](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/domain/viewmodel/list/PostListViewModel.kt)
  posts a side effect which
  [PostListScreen](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/app/features/postlist/ui/PostListScreen.kt)
  observes and sends to the `NavController`.

- The state is accessed in the screens through `Flow`.

- [PostListViewModel](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/domain/viewmodel/list/PostListViewModel.kt)
  and
  [PostDetailsViewModel](composeApp/src/commonMain/kotlin/org/orbitmvi/orbit/sample/posts/compose/multiplatform/domain/viewmodel/detail/PostDetailsViewModel.kt)
  use a `SavedStateHandle` for retaining the current state.

## Running the app

*Web*  
`./gradlew
samples:orbit-posts-compose-multiplatform:composeApp:wasmJsBrowserDevelopmentRun`

*Desktop*  
`./gradlew samples:orbit-posts-compose-multiplatform:composeApp:run`

*Android*  
Run from IDE

*iOS*  
Run from Xcode
