package com.kangraemin.stash.features.home

import android.os.Parcelable
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import kotlinx.parcelize.Parcelize

@Parcelize
data object HomeScreen : Screen, Parcelable {
    @Immutable
    data class State(
        val contents: List<SavedContent> = emptyList(),
        val selectedFilter: ContentType? = null,
        val error: String? = null,
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    sealed interface Event {
        data class OnFilterSelected(val type: ContentType?) : Event
        data class OnContentClicked(val content: SavedContent) : Event
        data object OnSearchClicked : Event
        data object OnSettingsClicked : Event
    }
}
