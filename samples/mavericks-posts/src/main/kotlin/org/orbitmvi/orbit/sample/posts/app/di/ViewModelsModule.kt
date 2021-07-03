package org.orbitmvi.orbit.sample.posts.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap
import org.orbitmvi.orbit.sample.posts.app.features.postdetails.viewmodel.PostDetailsViewModel
import org.orbitmvi.orbit.sample.posts.app.features.postlist.viewmodel.PostListViewModel

@Module
@InstallIn(MavericksViewModelComponent::class)
interface ViewModelsModule {
    @Binds
    @IntoMap
    @ViewModelKey(PostListViewModel::class)
    fun postListViewModelFactory(factory: PostListViewModel.Factory): AssistedViewModelFactory<*, *>

    @Binds
    @IntoMap
    @ViewModelKey(PostDetailsViewModel::class)
    fun postDetailsViewModelFactory(factory: PostDetailsViewModel.Factory): AssistedViewModelFactory<*, *>
}
