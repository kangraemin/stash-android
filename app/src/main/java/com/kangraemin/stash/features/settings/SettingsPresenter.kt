package com.kangraemin.stash.features.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.kangraemin.stash.domain.model.ThemeMode
import com.kangraemin.stash.domain.repository.ContentRepository
import com.kangraemin.stash.domain.repository.PreferencesRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val preferencesRepository: PreferencesRepository,
    private val contentRepository: ContentRepository,
) : Presenter<SettingsScreen.State> {

    @CircuitInject(SettingsScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): SettingsPresenter
    }

    @Composable
    override fun present(): SettingsScreen.State {
        var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
        var showDeleteAllDialog by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        val scope = rememberCoroutineScope()

        androidx.compose.runtime.LaunchedEffect(Unit) {
            try {
                preferencesRepository.getThemeMode().collect { themeMode = it }
            } catch (e: Exception) {
                error = e.message ?: "설정을 불러올 수 없습니다"
            }
        }

        return SettingsScreen.State(
            themeMode = themeMode,
            showDeleteAllDialog = showDeleteAllDialog,
            error = error,
        ) { event ->
            when (event) {
                is SettingsScreen.Event.OnThemeModeChanged -> {
                    scope.launch {
                        preferencesRepository.setThemeMode(event.mode)
                    }
                }
                is SettingsScreen.Event.OnDeleteAllClicked -> {
                    showDeleteAllDialog = true
                }
                is SettingsScreen.Event.OnDeleteAllConfirmed -> {
                    showDeleteAllDialog = false
                    scope.launch {
                        try {
                            contentRepository.deleteAll()
                        } catch (e: Exception) {
                            error = e.message ?: "삭제 중 오류가 발생했습니다"
                        }
                    }
                }
                is SettingsScreen.Event.OnDeleteAllDismissed -> {
                    showDeleteAllDialog = false
                }
                is SettingsScreen.Event.OnBackClicked -> {
                    navigator.pop()
                }
            }
        }
    }
}
