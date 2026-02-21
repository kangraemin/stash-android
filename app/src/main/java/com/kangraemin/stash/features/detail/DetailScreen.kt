package com.kangraemin.stash.features.detail

import android.os.Parcelable
import com.kangraemin.stash.domain.model.SavedContent
import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailScreen(val contentId: String) : Screen, Parcelable {
    @Immutable
    data class State(
        val content: SavedContent? = null,
        val showDeleteDialog: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event {
        data object OnOpenClicked : Event
        data object OnDeleteClicked : Event
        data object OnDeleteConfirmed : Event
        data object OnDeleteDismissed : Event
        data object OnBackClicked : Event
    }
}
