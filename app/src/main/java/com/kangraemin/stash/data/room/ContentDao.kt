package com.kangraemin.stash.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ContentEntity)

    @Update
    suspend fun update(entity: ContentEntity)

    @Query("SELECT * FROM saved_contents ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM saved_contents WHERE id = :id")
    suspend fun getById(id: String): ContentEntity?

    @Query("DELETE FROM saved_contents WHERE id = :id")
    suspend fun deleteById(id: String)
}
