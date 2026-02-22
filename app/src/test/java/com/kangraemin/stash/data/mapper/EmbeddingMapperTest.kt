package com.kangraemin.stash.data.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EmbeddingMapperTest {

    @Nested
    inner class `Float 리스트를 JSON 문자열로 변환` {
        @Test
        fun `기본 변환이 정확하다`() {
            val floats = listOf(1.0f, 2.5f, 3.14f)
            val json = floats.toJsonString()
            assertEquals("[1.0,2.5,3.14]", json)
        }

        @Test
        fun `빈 리스트는 빈 배열 문자열이 된다`() {
            val json = emptyList<Float>().toJsonString()
            assertEquals("[]", json)
        }

        @Test
        fun `단일 원소 변환이 정확하다`() {
            val json = listOf(0.5f).toJsonString()
            assertEquals("[0.5]", json)
        }
    }

    @Nested
    inner class `JSON 문자열을 Float 리스트로 변환` {
        @Test
        fun `기본 변환이 정확하다`() {
            val floats = "[1.0,2.5,3.14]".toFloatList()
            assertEquals(3, floats.size)
            assertEquals(1.0f, floats[0])
            assertEquals(2.5f, floats[1])
            assertEquals(3.14f, floats[2])
        }

        @Test
        fun `빈 배열 문자열은 빈 리스트가 된다`() {
            val floats = "[]".toFloatList()
            assertTrue(floats.isEmpty())
        }

        @Test
        fun `공백이 포함된 문자열도 파싱된다`() {
            val floats = "[1.0, 2.0, 3.0]".toFloatList()
            assertEquals(3, floats.size)
            assertEquals(1.0f, floats[0])
        }
    }

    @Nested
    inner class `양방향 변환` {
        @Test
        fun `리스트를 JSON으로 변환 후 다시 리스트로 변환하면 동일하다`() {
            val original = listOf(0.1f, 0.2f, 0.3f, -0.5f, 1.234f)
            val result = original.toJsonString().toFloatList()
            assertEquals(original.size, result.size)
            for (i in original.indices) {
                assertEquals(original[i], result[i], 0.0001f)
            }
        }
    }
}
