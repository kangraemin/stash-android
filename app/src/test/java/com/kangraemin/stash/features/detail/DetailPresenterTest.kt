package com.kangraemin.stash.features.detail

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.FakeContentRepository
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class DetailPresenterTest {

    private val testContent = SavedContent(
        id = "test-1",
        title = "테스트 콘텐츠",
        url = "https://example.com",
        contentType = ContentType.WEB,
        createdAt = Instant.ofEpochMilli(1700000000000L),
    )

    private class FakeNavigator : Navigator {
        var popCount = 0
        override fun goTo(screen: Screen): Boolean = true
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
    inner class `콘텐츠 로드` {
        @Test
        fun `화면 진입 시 contentId로 콘텐츠가 로드된다`() = runTest {
            val repository = FakeContentRepository(listOf(testContent))
            val navigator = FakeNavigator()
            val presenter = DetailPresenter(
                navigator = navigator,
                screen = DetailScreen(contentId = "test-1"),
                contentRepository = repository,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                assertNotNull(state.content)
                assertEquals("test-1", state.content?.id)
                assertEquals("테스트 콘텐츠", state.content?.title)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `존재하지 않는 contentId면 content가 null이다`() = runTest {
            val repository = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = DetailPresenter(
                navigator = navigator,
                screen = DetailScreen(contentId = "nonexistent"),
                contentRepository = repository,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                assertNull(state.content)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `삭제 다이얼로그` {
        @Test
        fun `OnDeleteClicked 시 showDeleteDialog가 true가 된다`() = runTest {
            val repository = FakeContentRepository(listOf(testContent))
            val navigator = FakeNavigator()
            val presenter = DetailPresenter(
                navigator = navigator,
                screen = DetailScreen(contentId = "test-1"),
                contentRepository = repository,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(DetailScreen.Event.OnDeleteClicked)

                state = expectMostRecentItem()
                assertTrue(state.showDeleteDialog)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `OnDeleteDismissed 시 showDeleteDialog가 false가 된다`() = runTest {
            val repository = FakeContentRepository(listOf(testContent))
            val navigator = FakeNavigator()
            val presenter = DetailPresenter(
                navigator = navigator,
                screen = DetailScreen(contentId = "test-1"),
                contentRepository = repository,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(DetailScreen.Event.OnDeleteClicked)
                state = expectMostRecentItem()

                state.eventSink(DetailScreen.Event.OnDeleteDismissed)
                state = expectMostRecentItem()
                assertFalse(state.showDeleteDialog)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `삭제 확인` {
        @Test
        fun `OnDeleteConfirmed 시 Repository에서 삭제되고 pop 호출된다`() = runTest {
            val repository = FakeContentRepository(listOf(testContent))
            val navigator = FakeNavigator()
            val presenter = DetailPresenter(
                navigator = navigator,
                screen = DetailScreen(contentId = "test-1"),
                contentRepository = repository,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                state.eventSink(DetailScreen.Event.OnDeleteConfirmed)

                // delete는 coroutine으로 실행되므로 잠시 대기
                testScheduler.advanceUntilIdle()

                assertNull(repository.getById("test-1"))
                assertEquals(1, navigator.popCount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `뒤로가기` {
        @Test
        fun `OnBackClicked 시 navigator pop이 호출된다`() = runTest {
            val repository = FakeContentRepository(listOf(testContent))
            val navigator = FakeNavigator()
            val presenter = DetailPresenter(
                navigator = navigator,
                screen = DetailScreen(contentId = "test-1"),
                contentRepository = repository,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                state.eventSink(DetailScreen.Event.OnBackClicked)

                assertEquals(1, navigator.popCount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
