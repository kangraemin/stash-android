package com.kangraemin.stash.data.mapper

import com.kangraemin.stash.data.room.ContentEntity
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class ContentMapperTest {

    @Nested
    inner class `Entity에서 Domain으로 변환` {
        @Test
        fun `기본 필드가 정확하게 변환된다`() {
            val entity = ContentEntity(
                id = "test-id",
                title = "테스트 제목",
                url = "https://example.com",
                contentType = "WEB",
                thumbnailUrl = "https://example.com/thumb.jpg",
                description = "테스트 설명",
                createdAt = 1700000000000L,
            )

            val domain = entity.toDomain()

            assertEquals("test-id", domain.id)
            assertEquals("테스트 제목", domain.title)
            assertEquals("https://example.com", domain.url)
            assertEquals(ContentType.WEB, domain.contentType)
            assertEquals("https://example.com/thumb.jpg", domain.thumbnailUrl)
            assertEquals("테스트 설명", domain.description)
            assertEquals(Instant.ofEpochMilli(1700000000000L), domain.createdAt)
        }

        @Test
        fun `nullable 필드가 null일 때 정확하게 변환된다`() {
            val entity = ContentEntity(
                id = "test-id",
                title = "제목",
                url = "https://example.com",
                contentType = "YOUTUBE",
                thumbnailUrl = null,
                description = null,
                createdAt = 1700000000000L,
            )

            val domain = entity.toDomain()

            assertNull(domain.thumbnailUrl)
            assertNull(domain.description)
        }

        @Test
        fun `모든 ContentType enum이 정확하게 변환된다`() {
            ContentType.entries.forEach { type ->
                val entity = ContentEntity(
                    id = "id",
                    title = "title",
                    url = "url",
                    contentType = type.name,
                    thumbnailUrl = null,
                    description = null,
                    createdAt = 0L,
                )
                assertEquals(type, entity.toDomain().contentType)
            }
        }

        @Test
        fun `Instant가 epoch millis에서 정확하게 변환된다`() {
            val epochMillis = 1609459200000L // 2021-01-01T00:00:00Z
            val entity = ContentEntity(
                id = "id",
                title = "title",
                url = "url",
                contentType = "WEB",
                thumbnailUrl = null,
                description = null,
                createdAt = epochMillis,
            )

            assertEquals(Instant.ofEpochMilli(epochMillis), entity.toDomain().createdAt)
        }
    }

    @Nested
    inner class `Domain에서 Entity로 변환` {
        @Test
        fun `기본 필드가 정확하게 변환된다`() {
            val createdAt = Instant.ofEpochMilli(1700000000000L)
            val domain = SavedContent(
                id = "test-id",
                title = "테스트 제목",
                url = "https://example.com",
                contentType = ContentType.INSTAGRAM,
                thumbnailUrl = "https://example.com/thumb.jpg",
                description = "테스트 설명",
                createdAt = createdAt,
            )

            val entity = domain.toEntity()

            assertEquals("test-id", entity.id)
            assertEquals("테스트 제목", entity.title)
            assertEquals("https://example.com", entity.url)
            assertEquals("INSTAGRAM", entity.contentType)
            assertEquals("https://example.com/thumb.jpg", entity.thumbnailUrl)
            assertEquals("테스트 설명", entity.description)
            assertEquals(1700000000000L, entity.createdAt)
        }

        @Test
        fun `nullable 필드가 null일 때 정확하게 변환된다`() {
            val domain = SavedContent(
                id = "test-id",
                title = "제목",
                url = "https://example.com",
                contentType = ContentType.WEB,
                thumbnailUrl = null,
                description = null,
                createdAt = Instant.now(),
            )

            val entity = domain.toEntity()

            assertNull(entity.thumbnailUrl)
            assertNull(entity.description)
        }

        @Test
        fun `ContentType이 name 문자열로 변환된다`() {
            ContentType.entries.forEach { type ->
                val domain = SavedContent(
                    id = "id",
                    title = "title",
                    url = "url",
                    contentType = type,
                    createdAt = Instant.now(),
                )
                assertEquals(type.name, domain.toEntity().contentType)
            }
        }

        @Test
        fun `Instant가 epoch millis로 정확하게 변환된다`() {
            val epochMillis = 1609459200000L
            val instant = Instant.ofEpochMilli(epochMillis)
            val domain = SavedContent(
                id = "id",
                title = "title",
                url = "url",
                contentType = ContentType.WEB,
                createdAt = instant,
            )

            assertEquals(epochMillis, domain.toEntity().createdAt)
        }
    }

    @Nested
    inner class `양방향 변환` {
        @Test
        fun `Domain을 Entity로 변환 후 다시 Domain으로 변환하면 동일하다`() {
            val original = SavedContent(
                id = "round-trip-id",
                title = "양방향 테스트",
                url = "https://example.com",
                contentType = ContentType.NAVER_MAP,
                thumbnailUrl = "https://example.com/thumb.jpg",
                description = "설명",
                createdAt = Instant.ofEpochMilli(1700000000000L),
            )

            val result = original.toEntity().toDomain()

            assertEquals(original, result)
        }
    }
}
