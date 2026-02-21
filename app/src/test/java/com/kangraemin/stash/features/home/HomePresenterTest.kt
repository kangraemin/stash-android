package com.kangraemin.stash.features.home

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.FakeContentRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class HomePresenterTest {

    private val webContent = SavedContent(
        id = "web-1",
        title = "웹 콘텐츠",
        url = "https://example.com",
        contentType = ContentType.WEB,
        createdAt = Instant.ofEpochMilli(1700000000000L),
    )

    private val youtubeContent = SavedContent(
        id = "youtube-1",
        title = "유튜브 콘텐츠",
        url = "https://youtube.com/watch?v=123",
        contentType = ContentType.YOUTUBE,
        createdAt = Instant.ofEpochMilli(1700000001000L),
    )

    @Nested
    inner class `콘텐츠 로드` {
        @Test
        fun `콘텐츠 로드 시 목록에 표시된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val presenter = HomePresenter(contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                assertEquals(2, state.contents.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `콘텐츠가 없으면 빈 목록이 반환된다`() = runTest {
            val repository = FakeContentRepository()
            val presenter = HomePresenter(contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                assertTrue(state.contents.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `필터링` {
        @Test
        fun `필터 선택 시 해당 타입만 표시된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val presenter = HomePresenter(contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(HomeScreen.Event.OnFilterSelected(ContentType.WEB))

                state = expectMostRecentItem()
                assertEquals(1, state.contents.size)
                assertEquals(ContentType.WEB, state.contents.first().contentType)
                assertEquals(ContentType.WEB, state.selectedFilter)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `전체 필터 선택 시 모든 콘텐츠가 표시된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val presenter = HomePresenter(contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(HomeScreen.Event.OnFilterSelected(ContentType.WEB))
                state = expectMostRecentItem()

                state.eventSink(HomeScreen.Event.OnFilterSelected(null))
                state = expectMostRecentItem()

                assertEquals(2, state.contents.size)
                assertNull(state.selectedFilter)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
