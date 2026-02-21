package com.kangraemin.stash.domain.repository

import com.kangraemin.stash.domain.model.SavedContent
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    suspend fun save(content: SavedContent)
    fun getAll(): Flow<List<SavedContent>>
    suspend fun getById(id: String): SavedContent?
    suspend fun delete(id: String)
}
