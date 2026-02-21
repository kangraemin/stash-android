package com.kangraemin.stash.features.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import com.kangraemin.stash.features.detail.DetailScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

class SearchPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val contentRepository: ContentRepository,
) : Presenter<SearchScreen.State> {

    @CircuitInject(SearchScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): SearchPresenter
    }

    @Composable
    override fun present(): SearchScreen.State {
        var query by remember { mutableStateOf("") }
        var results by remember { mutableStateOf<List<SavedContent>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }

        LaunchedEffect(query) {
            if (query.isBlank()) {
                results = emptyList()
                isLoading = false
                return@LaunchedEffect
            }
            isLoading = true
            delay(300)
            contentRepository.searchByKeyword(query).collect {
                results = it
                isLoading = false
            }
        }

        return SearchScreen.State(
            query = query,
            results = results,
            isLoading = isLoading,
        ) { event ->
            when (event) {
                is SearchScreen.Event.OnQueryChanged -> query = event.query
                is SearchScreen.Event.OnResultClicked -> {
                    navigator.goTo(DetailScreen(event.content.id))
                }
                is SearchScreen.Event.OnBackClicked -> navigator.pop()
            }
        }
    }
}
