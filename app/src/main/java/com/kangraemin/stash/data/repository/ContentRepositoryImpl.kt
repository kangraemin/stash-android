package com.kangraemin.stash.data.repository

import com.kangraemin.stash.data.mapper.toDomain
import com.kangraemin.stash.data.mapper.toEntity
import com.kangraemin.stash.data.room.ContentDao
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContentRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
) : ContentRepository {

    override suspend fun save(content: SavedContent) {
        contentDao.insert(content.toEntity())
    }

    override suspend fun update(content: SavedContent) {
        contentDao.update(content.toEntity())
    }

    override fun getAll(): Flow<List<SavedContent>> {
        return contentDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: String): SavedContent? {
        return contentDao.getById(id)?.toDomain()
    }

    override suspend fun delete(id: String) {
        contentDao.deleteById(id)
    }
}
