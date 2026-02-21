package com.kangraemin.stash.features.settings

import android.os.Parcelable
import com.kangraemin.stash.domain.model.ThemeMode
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object SettingsScreen : Screen, Parcelable {
    data class State(
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val showDeleteAllDialog: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event {
        data class OnThemeModeChanged(val mode: ThemeMode) : Event
        data object OnDeleteAllClicked : Event
        data object OnDeleteAllConfirmed : Event
        data object OnDeleteAllDismissed : Event
        data object OnBackClicked : Event
    }
}
