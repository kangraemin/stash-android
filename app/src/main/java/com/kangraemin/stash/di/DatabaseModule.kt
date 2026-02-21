package com.kangraemin.stash.di

import android.app.Application
import androidx.room.Room
import com.kangraemin.stash.data.room.ContentDao
import com.kangraemin.stash.data.room.StashDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): StashDatabase {
        return Room.databaseBuilder(
            app,
            StashDatabase::class.java,
            "stash.db",
        ).build()
    }

    @Provides
    fun provideContentDao(db: StashDatabase): ContentDao {
        return db.contentDao()
    }
}
