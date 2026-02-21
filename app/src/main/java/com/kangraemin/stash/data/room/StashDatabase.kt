package com.kangraemin.stash.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ContentEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class StashDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_contents ADD COLUMN embedding TEXT DEFAULT NULL")
            }
        }
    }
}
