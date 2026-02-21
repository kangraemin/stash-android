package com.kangraemin.stash.features.home

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kangraemin.stash.ui.theme.StashTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import dagger.hilt.components.SingletonComponent
import kotlinx.parcelize.Parcelize

@Parcelize
data object PlaceholderScreen : Screen, Parcelable {
    data object State : CircuitUiState
}

@CircuitInject(PlaceholderScreen::class, SingletonComponent::class)
class PlaceholderPresenter : Presenter<PlaceholderScreen.State> {
    @Composable
    override fun present(): PlaceholderScreen.State {
        return PlaceholderScreen.State
    }
}

@CircuitInject(PlaceholderScreen::class, SingletonComponent::class)
@Composable
fun Placeholder(state: PlaceholderScreen.State, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Stash",
            style = MaterialTheme.typography.headlineLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreview() {
    StashTheme {
        Placeholder(state = PlaceholderScreen.State)
    }
}
