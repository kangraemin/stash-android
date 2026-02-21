package com.kangraemin.stash.features.settings

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.model.ThemeMode
import com.kangraemin.stash.domain.repository.FakeContentRepository
import com.kangraemin.stash.domain.repository.FakePreferencesRepository
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant

class SettingsPresenterTest {

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

    private val testContent = SavedContent(
        id = "test-1",
        title = "테스트",
        url = "https://example.com",
        contentType = ContentType.WEB,
        createdAt = Instant.ofEpochMilli(1700000000000L),
    )

    @Nested
    inner class `테마 모드` {
        @Test
        fun `초기 테마 모드는 SYSTEM이다`() = runTest {
            val preferencesRepo = FakePreferencesRepository()
            val contentRepo = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = SettingsPresenter(
                navigator = navigator,
                preferencesRepository = preferencesRepo,
                contentRepository = contentRepo,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                assertEquals(ThemeMode.SYSTEM, state.themeMode)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `테마 모드 변경 시 상태가 업데이트된다`() = runTest {
            val preferencesRepo = FakePreferencesRepository()
            val contentRepo = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = SettingsPresenter(
                navigator = navigator,
                preferencesRepository = preferencesRepo,
                contentRepository = contentRepo,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SettingsScreen.Event.OnThemeModeChanged(ThemeMode.DARK))

                testScheduler.advanceUntilIdle()
                state = expectMostRecentItem()
                assertEquals(ThemeMode.DARK, state.themeMode)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `전체 삭제` {
        @Test
        fun `삭제 버튼 클릭 시 확인 다이얼로그가 표시된다`() = runTest {
            val preferencesRepo = FakePreferencesRepository()
            val contentRepo = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = SettingsPresenter(
                navigator = navigator,
                preferencesRepository = preferencesRepo,
                contentRepository = contentRepo,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SettingsScreen.Event.OnDeleteAllClicked)

                state = expectMostRecentItem()
                assertTrue(state.showDeleteAllDialog)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `삭제 취소 시 다이얼로그가 닫힌다`() = runTest {
            val preferencesRepo = FakePreferencesRepository()
            val contentRepo = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = SettingsPresenter(
                navigator = navigator,
                preferencesRepository = preferencesRepo,
                contentRepository = contentRepo,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SettingsScreen.Event.OnDeleteAllClicked)
                state = expectMostRecentItem()

                state.eventSink(SettingsScreen.Event.OnDeleteAllDismissed)
                state = expectMostRecentItem()
                assertFalse(state.showDeleteAllDialog)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `삭제 확인 시 모든 콘텐츠가 삭제된다`() = runTest {
            val preferencesRepo = FakePreferencesRepository()
            val contentRepo = FakeContentRepository(listOf(testContent))
            val navigator = FakeNavigator()
            val presenter = SettingsPresenter(
                navigator = navigator,
                preferencesRepository = preferencesRepo,
                contentRepository = contentRepo,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                var state = expectMostRecentItem()
                state.eventSink(SettingsScreen.Event.OnDeleteAllClicked)
                state = expectMostRecentItem()

                state.eventSink(SettingsScreen.Event.OnDeleteAllConfirmed)

                testScheduler.advanceUntilIdle()
                state = expectMostRecentItem()
                assertFalse(state.showDeleteAllDialog)
                assertNull(contentRepo.getById("test-1"))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `뒤로가기` {
        @Test
        fun `OnBackClicked 시 navigator pop이 호출된다`() = runTest {
            val preferencesRepo = FakePreferencesRepository()
            val contentRepo = FakeContentRepository()
            val navigator = FakeNavigator()
            val presenter = SettingsPresenter(
                navigator = navigator,
                preferencesRepository = preferencesRepo,
                contentRepository = contentRepo,
            )

            moleculeFlow(RecompositionMode.Immediate) {
                presenter.present()
            }.test {
                val state = expectMostRecentItem()
                state.eventSink(SettingsScreen.Event.OnBackClicked)

                assertEquals(1, navigator.popCount)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
