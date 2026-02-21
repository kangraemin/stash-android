package com.kangraemin.stash.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.kangraemin.stash.data.repository.PreferencesRepositoryImpl
import com.kangraemin.stash.domain.repository.PreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesDataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(app: Application): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            app.preferencesDataStoreFile("stash_preferences")
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesRepositoryModule {
    @Binds
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
