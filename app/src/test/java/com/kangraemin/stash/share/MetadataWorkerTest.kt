package com.kangraemin.stash.share

import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.FakeContentRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class MetadataWorkerTest {

    private lateinit var repository: FakeContentRepository

    private val testContent = SavedContent(
        id = "test-id",
        title = "https://example.com",
        url = "https://example.com",
        contentType = ContentType.WEB,
        thumbnailUrl = null,
        description = null,
        createdAt = Instant.ofEpochMilli(1700000000000L),
    )

    @BeforeEach
    fun setUp() {
        repository = FakeContentRepository(listOf(testContent))
    }

    @Nested
    inner class `메타데이터 업데이트 로직` {
        @Test
        fun `contentId로 콘텐츠를 조회할 수 있다`() = runTest {
            val content = repository.getById("test-id")

            assertEquals(testContent, content)
        }

        @Test
        fun `존재하지 않는 contentId는 null을 반환한다`() = runTest {
            val content = repository.getById("non-existent")

            assertNull(content)
        }

        @Test
        fun `메타데이터를 업데이트하면 제목이 변경된다`() = runTest {
            val updated = testContent.copy(
                title = "Example Page Title",
                description = "A description from OG tags",
                thumbnailUrl = "https://example.com/og-image.jpg",
            )

            repository.update(updated)

            val result = repository.getById("test-id")
            assertEquals("Example Page Title", result?.title)
            assertEquals("A description from OG tags", result?.description)
            assertEquals("https://example.com/og-image.jpg", result?.thumbnailUrl)
        }

        @Test
        fun `메타데이터 업데이트 시 다른 필드는 변경되지 않는다`() = runTest {
            val updated = testContent.copy(
                title = "Updated Title",
            )

            repository.update(updated)

            val result = repository.getById("test-id")
            assertEquals("https://example.com", result?.url)
            assertEquals(ContentType.WEB, result?.contentType)
            assertEquals(Instant.ofEpochMilli(1700000000000L), result?.createdAt)
        }
    }
}
