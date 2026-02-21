package com.kangraemin.stash.features.search

import android.os.Parcelable
import com.kangraemin.stash.domain.model.SavedContent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object SearchScreen : Screen, Parcelable {
    data class State(
        val query: String = "",
        val results: List<SavedContent> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event {
        data class OnQueryChanged(val query: String) : Event
        data class OnResultClicked(val content: SavedContent) : Event
        data object OnBackClicked : Event
    }
}
