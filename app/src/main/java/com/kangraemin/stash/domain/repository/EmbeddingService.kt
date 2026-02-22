package com.kangraemin.stash.domain.repository

interface EmbeddingService {
    suspend fun generateEmbedding(text: String): List<Float>
}
