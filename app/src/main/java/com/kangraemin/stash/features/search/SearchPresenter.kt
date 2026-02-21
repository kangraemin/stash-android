package com.kangraemin.stash.features.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import com.kangraemin.stash.domain.repository.VectorSearchService
import com.kangraemin.stash.features.detail.DetailScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SearchPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val contentRepository: ContentRepository,
    private val vectorSearchService: VectorSearchService,
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
            results = hybridSearch(query)
            isLoading = false
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

    private suspend fun hybridSearch(query: String): List<SavedContent> = coroutineScope {
        val keywordDeferred = async {
            runCatching { contentRepository.searchByKeyword(query).first() }.getOrDefault(emptyList())
        }
        val semanticDeferred = async {
            runCatching { vectorSearchService.searchBySimilarity(query) }.getOrDefault(emptyList())
        }

        val keywordResults = keywordDeferred.await()
        val semanticResults = semanticDeferred.await()

        mergeResults(keywordResults, semanticResults)
    }

    private fun mergeResults(
        keywordResults: List<SavedContent>,
        semanticResults: List<SavedContent>,
    ): List<SavedContent> {
        val seen = keywordResults.map { it.id }.toMutableSet()
        val merged = keywordResults.toMutableList()
        for (content in semanticResults) {
            if (content.id !in seen) {
                seen.add(content.id)
                merged.add(content)
            }
        }
        return merged
    }
}
