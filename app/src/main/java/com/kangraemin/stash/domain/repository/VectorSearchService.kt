package com.kangraemin.stash.domain.repository

import com.kangraemin.stash.domain.model.SavedContent

interface VectorSearchService {
    suspend fun searchBySimilarity(query: String, topK: Int = DEFAULT_TOP_K): List<SavedContent>

    companion object {
        const val DEFAULT_TOP_K = 10
    }
}
