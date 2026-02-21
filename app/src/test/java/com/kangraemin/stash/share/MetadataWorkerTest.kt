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

    @BeforeEach
    fun setUp() {
        repository = FakeContentRepository()
    }

    @Nested
    inner class `메타데이터 업데이트 흐름` {
        @Test
        fun `메타데이터 업데이트 시 title이 갱신된다`() = runTest {
            val content = createContent("test-id", "https://example.com")
            repository.save(content)

            val updated = content.copy(
                title = "Example Page Title",
                description = "Example description",
                thumbnailUrl = "https://example.com/image.jpg",
            )
            repository.update(updated)

            val result = repository.getById("test-id")
            assertEquals("Example Page Title", result?.title)
            assertEquals("Example description", result?.description)
            assertEquals("https://example.com/image.jpg", result?.thumbnailUrl)
        }

        @Test
        fun `메타데이터 업데이트 시 기존 URL과 contentType은 유지된다`() = runTest {
            val content = createContent(
                id = "test-id",
                url = "https://www.youtube.com/watch?v=abc",
                contentType = ContentType.YOUTUBE,
            )
            repository.save(content)

            val updated = content.copy(
                title = "YouTube Video Title",
                description = "Video description",
            )
            repository.update(updated)

            val result = repository.getById("test-id")
            assertEquals("https://www.youtube.com/watch?v=abc", result?.url)
            assertEquals(ContentType.YOUTUBE, result?.contentType)
        }

        @Test
        fun `존재하지 않는 contentId로 업데이트하면 변경되지 않는다`() = runTest {
            val content = createContent("test-id", "https://example.com")
            repository.save(content)

            val nonExistent = content.copy(id = "non-existent", title = "Updated")
            repository.update(nonExistent)

            val result = repository.getById("test-id")
            assertEquals("https://example.com", result?.title)
            assertNull(repository.getById("non-existent"))
        }
    }

    @Nested
    inner class `부분 메타데이터 업데이트` {
        @Test
        fun `title만 있는 경우 title만 업데이트된다`() = runTest {
            val content = createContent("test-id", "https://example.com")
            repository.save(content)

            val updated = content.copy(title = "Page Title")
            repository.update(updated)

            val result = repository.getById("test-id")
            assertEquals("Page Title", result?.title)
            assertNull(result?.description)
            assertNull(result?.thumbnailUrl)
        }

        @Test
        fun `thumbnailUrl만 있는 경우 thumbnailUrl만 업데이트된다`() = runTest {
            val content = createContent("test-id", "https://example.com")
            repository.save(content)

            val updated = content.copy(
                thumbnailUrl = "https://example.com/thumb.jpg",
            )
            repository.update(updated)

            val result = repository.getById("test-id")
            assertEquals("https://example.com/thumb.jpg", result?.thumbnailUrl)
        }
    }

    private fun createContent(
        id: String,
        url: String,
        contentType: ContentType = ContentType.WEB,
    ) = SavedContent(
        id = id,
        title = url,
        url = url,
        contentType = contentType,
        thumbnailUrl = null,
        description = null,
        createdAt = Instant.now(),
    )
}
