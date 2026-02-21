package com.kangraemin.stash.share

import com.kangraemin.stash.domain.contentparsing.UrlParser
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.FakeContentRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ShareSaveLogicTest {

    private lateinit var repository: FakeContentRepository

    @BeforeEach
    fun setUp() {
        repository = FakeContentRepository()
    }

    @Nested
    inner class `URL 수신 후 저장 흐름` {
        @Test
        fun `유튜브 URL을 저장하면 YOUTUBE 타입으로 저장된다`() = runTest {
            val url = "https://www.youtube.com/watch?v=abc123"
            saveContent(url)

            val saved = repository.getAll().first()
            assertEquals(1, saved.size)
            assertEquals(ContentType.YOUTUBE, saved[0].contentType)
            assertEquals(url, saved[0].url)
        }

        @Test
        fun `인스타그램 URL을 저장하면 INSTAGRAM 타입으로 저장된다`() = runTest {
            val url = "https://www.instagram.com/p/abc123"
            saveContent(url)

            val saved = repository.getAll().first()
            assertEquals(ContentType.INSTAGRAM, saved[0].contentType)
        }

        @Test
        fun `일반 URL을 저장하면 WEB 타입으로 저장된다`() = runTest {
            val url = "https://www.example.com/article/123"
            saveContent(url)

            val saved = repository.getAll().first()
            assertEquals(ContentType.WEB, saved[0].contentType)
        }

        @Test
        fun `저장 시 URL이 임시 제목으로 사용된다`() = runTest {
            val url = "https://www.youtube.com/watch?v=abc123"
            saveContent(url)

            val saved = repository.getAll().first()
            assertEquals(url, saved[0].title)
        }

        @Test
        fun `저장 시 고유 ID가 생성된다`() = runTest {
            saveContent("https://www.example.com/1")
            saveContent("https://www.example.com/2")

            val saved = repository.getAll().first()
            assertEquals(2, saved.size)
            assertNotNull(saved[0].id)
            assertNotNull(saved[1].id)
            assert(saved[0].id != saved[1].id)
        }

        @Test
        fun `저장 시 createdAt이 설정된다`() = runTest {
            val before = Instant.now()
            saveContent("https://www.example.com")
            val after = Instant.now()

            val saved = repository.getAll().first()
            assert(!saved[0].createdAt.isBefore(before))
            assert(!saved[0].createdAt.isAfter(after))
        }
    }

    @Nested
    inner class `ContentType 매핑 및 저장 통합` {
        @Test
        fun `네이버 지도 URL이 올바르게 저장된다`() = runTest {
            saveContent("https://map.naver.com/v5/entry/place/12345")

            val saved = repository.getAll().first()
            assertEquals(ContentType.NAVER_MAP, saved[0].contentType)
        }

        @Test
        fun `구글 맵 URL이 올바르게 저장된다`() = runTest {
            saveContent("https://maps.app.goo.gl/abc123")

            val saved = repository.getAll().first()
            assertEquals(ContentType.GOOGLE_MAP, saved[0].contentType)
        }

        @Test
        fun `쿠팡 URL이 올바르게 저장된다`() = runTest {
            saveContent("https://www.coupang.com/vp/products/12345")

            val saved = repository.getAll().first()
            assertEquals(ContentType.COUPANG, saved[0].contentType)
        }
    }

    private suspend fun saveContent(url: String) {
        val contentType = UrlParser.parseContentType(url)
        val content = SavedContent(
            id = UUID.randomUUID().toString(),
            url = url,
            contentType = contentType,
            title = url,
            createdAt = Instant.now(),
        )
        repository.save(content)
    }
}
