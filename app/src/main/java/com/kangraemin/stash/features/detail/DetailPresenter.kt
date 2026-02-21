package com.kangraemin.stash.features.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.domain.repository.ContentRepository
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

class DetailPresenter @AssistedInject constructor(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: DetailScreen,
    private val contentRepository: ContentRepository,
) : Presenter<DetailScreen.State> {

    @CircuitInject(DetailScreen::class, SingletonComponent::class)
    @AssistedFactory
    fun interface Factory {
        fun create(navigator: Navigator, screen: DetailScreen): DetailPresenter
    }

    @Composable
    override fun present(): DetailScreen.State {
        var content by remember { mutableStateOf<SavedContent?>(null) }
        var showDeleteDialog by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            content = contentRepository.getById(screen.contentId)
        }

        return DetailScreen.State(
            content = content,
            showDeleteDialog = showDeleteDialog,
        ) { event ->
            when (event) {
                is DetailScreen.Event.OnOpenClicked -> Unit
                is DetailScreen.Event.OnDeleteClicked -> showDeleteDialog = true
                is DetailScreen.Event.OnDeleteConfirmed -> {
                    scope.launch {
                        contentRepository.delete(screen.contentId)
                        navigator.pop()
                    }
                }
                is DetailScreen.Event.OnDeleteDismissed -> showDeleteDialog = false
                is DetailScreen.Event.OnBackClicked -> navigator.pop()
            }
        }
    }
}
