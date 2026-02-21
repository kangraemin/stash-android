package com.kangraemin.stash.features.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import com.kangraemin.stash.features.detail.DetailScreen
import com.kangraemin.stash.features.search.SearchScreen
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent

class HomePresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    private val contentRepository: ContentRepository,
) : Presenter<HomeScreen.State> {

    @CircuitInject(HomeScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator): HomePresenter
    }

    @Composable
    override fun present(): HomeScreen.State {
        var contents by remember { mutableStateOf<List<SavedContent>>(emptyList()) }
        var selectedFilter by remember { mutableStateOf<ContentType?>(null) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                contentRepository.getAll().collect { contents = it }
            } catch (e: Exception) {
                error = e.message ?: "콘텐츠를 불러올 수 없습니다"
            }
        }

        return HomeScreen.State(
            contents = selectedFilter?.let { filter ->
                contents.filter { it.contentType == filter }
            } ?: contents,
            selectedFilter = selectedFilter,
            error = error,
        ) { event ->
            when (event) {
                is HomeScreen.Event.OnFilterSelected -> selectedFilter = event.type
                is HomeScreen.Event.OnContentClicked -> {
                    navigator.goTo(DetailScreen(event.content.id))
                }
                is HomeScreen.Event.OnSearchClicked -> {
                    navigator.goTo(SearchScreen)
                }
            }
        }
    }
}
