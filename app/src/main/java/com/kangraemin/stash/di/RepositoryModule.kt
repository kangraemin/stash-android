package com.kangraemin.stash.di

import com.kangraemin.stash.data.repository.ContentRepositoryImpl
import com.kangraemin.stash.domain.repository.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
