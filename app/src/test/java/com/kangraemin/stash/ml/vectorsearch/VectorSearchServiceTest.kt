package com.kangraemin.stash.ml.vectorsearch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.math.abs

class VectorSearchServiceTest {

    @Nested
    inner class `코사인 유사도 계산` {
        @Test
        fun `동일한 벡터의 유사도는 1이다`() {
            val vector = listOf(1.0f, 2.0f, 3.0f)
            val similarity = VectorSearchServiceImpl.cosineSimilarity(vector, vector)
            assertTrue(abs(similarity - 1.0f) < 0.0001f)
        }

        @Test
        fun `반대 방향 벡터의 유사도는 -1이다`() {
            val a = listOf(1.0f, 0.0f, 0.0f)
            val b = listOf(-1.0f, 0.0f, 0.0f)
            val similarity = VectorSearchServiceImpl.cosineSimilarity(a, b)
            assertTrue(abs(similarity - (-1.0f)) < 0.0001f)
        }

        @Test
        fun `직교 벡터의 유사도는 0이다`() {
            val a = listOf(1.0f, 0.0f, 0.0f)
            val b = listOf(0.0f, 1.0f, 0.0f)
            val similarity = VectorSearchServiceImpl.cosineSimilarity(a, b)
            assertTrue(abs(similarity) < 0.0001f)
        }

        @Test
        fun `빈 벡터의 유사도는 0이다`() {
            val similarity = VectorSearchServiceImpl.cosineSimilarity(emptyList(), emptyList())
            assertEquals(0f, similarity)
        }

        @Test
        fun `크기가 다른 벡터의 유사도는 0이다`() {
            val a = listOf(1.0f, 2.0f)
            val b = listOf(1.0f, 2.0f, 3.0f)
            val similarity = VectorSearchServiceImpl.cosineSimilarity(a, b)
            assertEquals(0f, similarity)
        }

        @Test
        fun `영벡터의 유사도는 0이다`() {
            val a = listOf(0.0f, 0.0f, 0.0f)
            val b = listOf(1.0f, 2.0f, 3.0f)
            val similarity = VectorSearchServiceImpl.cosineSimilarity(a, b)
            assertEquals(0f, similarity)
        }

        @Test
        fun `유사한 벡터는 높은 유사도를 가진다`() {
            val a = listOf(1.0f, 2.0f, 3.0f)
            val b = listOf(1.1f, 2.1f, 3.1f)
            val similarity = VectorSearchServiceImpl.cosineSimilarity(a, b)
            assertTrue(similarity > 0.99f)
        }
    }
}
