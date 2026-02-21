package com.kangraemin.stash.ml.vectorsearch

import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import com.kangraemin.stash.domain.repository.EmbeddingService
import com.kangraemin.stash.domain.repository.VectorSearchService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class VectorSearchServiceImpl @Inject constructor(
    private val embeddingService: EmbeddingService,
    private val contentRepository: ContentRepository,
) : VectorSearchService {

    override suspend fun searchBySimilarity(query: String, topK: Int): List<SavedContent> {
        val queryEmbedding = embeddingService.generateEmbedding(query)
        if (queryEmbedding.isEmpty()) return emptyList()

        val allContents = contentRepository.getAll().first()

        return allContents
            .filter { it.embedding != null }
            .map { content ->
                val similarity = cosineSimilarity(queryEmbedding, content.embedding!!)
                content to similarity
            }
            .sortedByDescending { it.second }
            .take(topK)
            .filter { it.second > SIMILARITY_THRESHOLD }
            .map { it.first }
    }

    companion object {
        private const val SIMILARITY_THRESHOLD = 0.3f

        fun cosineSimilarity(a: List<Float>, b: List<Float>): Float {
            if (a.size != b.size || a.isEmpty()) return 0f

            var dotProduct = 0f
            var normA = 0f
            var normB = 0f

            for (i in a.indices) {
                dotProduct += a[i] * b[i]
                normA += a[i] * a[i]
                normB += b[i] * b[i]
            }

            val denominator = sqrt(normA) * sqrt(normB)
            return if (denominator == 0f) 0f else dotProduct / denominator
        }
    }
}
