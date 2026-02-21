package com.kangraemin.stash.domain.repository

import com.kangraemin.stash.domain.model.SavedContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeContentRepository(
    contents: List<SavedContent> = emptyList(),
) : ContentRepository {

    private val _contents = MutableStateFlow(contents.toMutableList())

    override suspend fun save(content: SavedContent) {
        _contents.value = _contents.value.toMutableList().apply {
            removeAll { it.id == content.id }
            add(0, content)
        }
    }

    override suspend fun update(content: SavedContent) {
        _contents.value = _contents.value.toMutableList().apply {
            val index = indexOfFirst { it.id == content.id }
            if (index >= 0) set(index, content)
        }
    }

    override fun getAll(): Flow<List<SavedContent>> {
        return _contents.map { it.toList() }
    }

    override suspend fun getById(id: String): SavedContent? {
        return _contents.value.find { it.id == id }
    }

    override suspend fun delete(id: String) {
        _contents.value = _contents.value.toMutableList().apply {
            removeAll { it.id == id }
        }
    }
}
