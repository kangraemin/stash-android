package com.kangraemin.stash.data.repository

import app.cash.turbine.test
import com.kangraemin.stash.data.room.ContentDao
import com.kangraemin.stash.data.room.ContentEntity
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class ContentRepositoryImplTest {

    private lateinit var fakeDao: FakeContentDao
    private lateinit var repository: ContentRepositoryImpl

    @BeforeEach
    fun setUp() {
        fakeDao = FakeContentDao()
        repository = ContentRepositoryImpl(contentDao = fakeDao)
    }

    @Nested
    inner class `저장 테스트` {
        @Test
        fun `콘텐츠를 저장하면 DAO에 Entity가 저장된다`() = runTest {
            val content = SavedContent(
                id = "test-id",
                title = "테스트",
                url = "https://example.com",
                contentType = ContentType.WEB,
                thumbnailUrl = null,
                description = null,
                createdAt = Instant.ofEpochMilli(1700000000000L),
            )

            repository.save(content)

            val saved = fakeDao.getById("test-id")
            assertEquals("test-id", saved?.id)
            assertEquals("테스트", saved?.title)
            assertEquals("WEB", saved?.contentType)
        }
    }

    @Nested
    inner class `조회 테스트` {
        @Test
        fun `ID로 저장된 콘텐츠를 조회할 수 있다`() = runTest {
            val content = SavedContent(
                id = "test-id",
                title = "테스트",
                url = "https://example.com",
                contentType = ContentType.YOUTUBE,
                thumbnailUrl = "https://example.com/thumb.jpg",
                description = "설명",
                createdAt = Instant.ofEpochMilli(1700000000000L),
            )
            repository.save(content)

            val result = repository.getById("test-id")

            assertEquals(content, result)
        }

        @Test
        fun `존재하지 않는 ID로 조회하면 null을 반환한다`() = runTest {
            val result = repository.getById("non-existent")

            assertNull(result)
        }

        @Test
        fun `getAll은 저장된 모든 콘텐츠를 Flow로 반환한다`() = runTest {
            val content1 = SavedContent(
                id = "id-1",
                title = "콘텐츠 1",
                url = "https://example.com/1",
                contentType = ContentType.WEB,
                createdAt = Instant.ofEpochMilli(1700000000000L),
            )
            val content2 = SavedContent(
                id = "id-2",
                title = "콘텐츠 2",
                url = "https://example.com/2",
                contentType = ContentType.INSTAGRAM,
                createdAt = Instant.ofEpochMilli(1700000001000L),
            )
            repository.save(content1)
            repository.save(content2)

            repository.getAll().test {
                val items = awaitItem()
                assertEquals(2, items.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `삭제 테스트` {
        @Test
        fun `콘텐츠를 삭제하면 조회되지 않는다`() = runTest {
            val content = SavedContent(
                id = "test-id",
                title = "테스트",
                url = "https://example.com",
                contentType = ContentType.WEB,
                createdAt = Instant.ofEpochMilli(1700000000000L),
            )
            repository.save(content)

            repository.delete("test-id")

            assertNull(repository.getById("test-id"))
        }

        @Test
        fun `삭제 후 getAll에서도 제외된다`() = runTest {
            val content = SavedContent(
                id = "test-id",
                title = "테스트",
                url = "https://example.com",
                contentType = ContentType.WEB,
                createdAt = Instant.ofEpochMilli(1700000000000L),
            )
            repository.save(content)
            repository.delete("test-id")

            repository.getAll().test {
                val items = awaitItem()
                assertEquals(0, items.size)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}

private class FakeContentDao : ContentDao {
    private val entities = MutableStateFlow<List<ContentEntity>>(emptyList())

    override suspend fun insert(entity: ContentEntity) {
        entities.value = entities.value.toMutableList().apply {
            removeAll { it.id == entity.id }
            add(entity)
        }
    }

    override suspend fun update(entity: ContentEntity) {
        entities.value = entities.value.toMutableList().apply {
            val index = indexOfFirst { it.id == entity.id }
            if (index >= 0) set(index, entity)
        }
    }

    override fun getAll(): Flow<List<ContentEntity>> {
        return entities.map { it.sortedByDescending { e -> e.createdAt } }
    }

    override suspend fun getById(id: String): ContentEntity? {
        return entities.value.find { it.id == id }
    }

    override suspend fun deleteById(id: String) {
        entities.value = entities.value.toMutableList().apply {
            removeAll { it.id == id }
        }
    }
}
