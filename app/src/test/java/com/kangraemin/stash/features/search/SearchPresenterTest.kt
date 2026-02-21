package com.kangraemin.stash.features.search

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.FakeContentRepository
import com.kangraemin.stash.features.detail.DetailScreen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SearchPresenterTest {

    private val webContent = SavedContent(
        id = "web-1",
        title = "웹 콘텐츠",
        url = "https://example.com",
        contentType = ContentType.WEB,
        description = "웹 페이지 설명",
        createdAt = Instant.ofEpochMilli(1700000000000L),
    )

    private val youtubeContent = SavedContent(
        id = "youtube-1",
        title = "유튜브 영상",
        url = "https://youtube.com/watch?v=123",
        contentType = ContentType.YOUTUBE,
        description = "유튜브 영상 설명",
        createdAt = Instant.ofEpochMilli(1700000001000L),
    )

    private class FakeNavigator : Navigator {
        val screens = mutableListOf<Screen>()
        var popCount = 0
        override fun goTo(screen: Screen): Boolean {
            screens.add(screen)
            return true
        }
        override fun pop(result: PopResult?): Screen? {
            popCount++
            return null
        }
        override fun resetRoot(
            newRoot: Screen,
            saveState: Boolean,
            restoreState: Boolean,
        ): ImmutableList<Screen> = persistentListOf()
        override fun peek(): Screen? = null
        override fun peekBackStack(): ImmutableList<Screen> = persistentListOf()
    }

    @Nested
    inner class `키워드 검색` {
        @Test
        fun `키워드 입력 시 매칭되는 결과가 표시된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val navigator = FakeNavigator()
            val presenter = SearchPresenter(navigator = navigator, contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SearchScreen.Event.OnQueryChanged("유튜브"))

                testScheduler.advanceTimeBy(350)
                testScheduler.runCurrent()

                state = expectMostRecentItem()
                assertEquals(1, state.results.size)
                assertEquals("youtube-1", state.results.first().id)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `빈 쿼리 입력 시 빈 결과가 반환된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val navigator = FakeNavigator()
            val presenter = SearchPresenter(navigator = navigator, contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                assertTrue(state.results.isEmpty())
                assertEquals("", state.query)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `쿼리 변경 시 새로운 결과로 업데이트된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val navigator = FakeNavigator()
            val presenter = SearchPresenter(navigator = navigator, contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SearchScreen.Event.OnQueryChanged("웹"))

                testScheduler.advanceTimeBy(350)
                testScheduler.runCurrent()

                state = expectMostRecentItem()
                assertEquals(1, state.results.size)
                assertEquals("web-1", state.results.first().id)

                state.eventSink(SearchScreen.Event.OnQueryChanged("유튜브"))

                testScheduler.advanceTimeBy(350)
                testScheduler.runCurrent()

                state = expectMostRecentItem()
                assertEquals(1, state.results.size)
                assertEquals("youtube-1", state.results.first().id)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `매칭 결과가 없으면 빈 목록이 반환된다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent, youtubeContent))
            val navigator = FakeNavigator()
            val presenter = SearchPresenter(navigator = navigator, contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SearchScreen.Event.OnQueryChanged("존재하지않는검색어"))

                testScheduler.advanceTimeBy(350)
                testScheduler.runCurrent()

                state = expectMostRecentItem()
                assertTrue(state.results.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `결과 클릭` {
        @Test
        fun `OnResultClicked 시 DetailScreen으로 이동한다`() = runTest {
            val repository = FakeContentRepository(listOf(webContent))
            val navigator = FakeNavigator()
            val presenter = SearchPresenter(navigator = navigator, contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                state.eventSink(SearchScreen.Event.OnResultClicked(webContent))

                assertEquals(1, navigator.screens.size)
                assertEquals(DetailScreen("web-1"), navigator.screens.first())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `뒤로가기` {
        @Test
        fun `OnBackClicked 시 navigator pop이 호출된다`() = runTest {
            val repository = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = SearchPresenter(navigator = navigator, contentRepository = repository)

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                state.eventSink(SearchScreen.Event.OnBackClicked)

                assertEquals(1, navigator.popCount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
