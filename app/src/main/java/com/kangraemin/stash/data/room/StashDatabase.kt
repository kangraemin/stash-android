package com.kangraemin.stash.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ContentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StashDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
}
