package com.kangraemin.stash.di

import com.kangraemin.stash.domain.repository.EmbeddingService
import com.kangraemin.stash.ml.embedding.TextEmbeddingService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {
    @Binds
    abstract fun bindEmbeddingService(impl: TextEmbeddingService): EmbeddingService
}
