import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    //@State
    var viewModel = PostListViewModel(
        savedStateHandle: Lifecycle_viewmodel_savedstateSavedStateHandle(),
        postRepository: PostDataRepository(
            networkDataSource: PostNetworkDataSource(client: HttpClientFactoryKt.httpClientFactory()),
            avatarUrlGenerator: AvatarUrlGenerator()
        )
    )
    
    var body: some Scene {
        WindowGroup {
            ScrollView {
                Observing(viewModel.container.stateFlow) { state in
                    let state = state as! PostListState
                    
                    LazyVGrid(columns: [GridItem(.flexible(minimum: 50, maximum: .infinity))]) {
                        let lastItem = state.overviews.last
                        
                        ForEach(state.overviews, id: \.self) { (postOverview:PostOverview) in
                            HStack {
                                AsyncImage(url: URL(string: postOverview.avatarUrl)){ image in
                                    image.resizable()
                                } placeholder: {
                                    Color.white
                                }.frame(width: 40, height: 40)
                                VStack(alignment: .leading) {
                                    Text(postOverview.username).font(.caption)
                                    Text(postOverview.title)
                                }
                                Spacer()
                            }
                            if (postOverview != lastItem) {
                                Divider()
                            }
                        }
                    }
                }
            }.onDisappear {
                viewModel.onCleared()
            }
            ContentView()
        }
    }
}
