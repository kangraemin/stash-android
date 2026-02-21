package com.kangraemin.stash.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kangraemin.stash.domain.model.ContentType
import com.kangraemin.stash.domain.model.SavedContent
import com.kangraemin.stash.ui.theme.StashTheme
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@CircuitInject(HomeScreen::class, SingletonComponent::class)
@Composable
fun Home(state: HomeScreen.State, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Stash") },
                actions = {
                    IconButton(onClick = { state.eventSink(HomeScreen.Event.OnSettingsClicked) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBar(
                onClick = { state.eventSink(HomeScreen.Event.OnSearchClicked) },
            )
            FilterChips(
                selected = state.selectedFilter,
                onSelected = { state.eventSink(HomeScreen.Event.OnFilterSelected(it)) },
            )
            if (state.contents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "저장된 콘텐츠가 없습니다.\n다른 앱에서 공유하여 콘텐츠를 저장해보세요.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.contents, key = { it.id }) { content ->
                        ContentCard(
                            content = content,
                            onClick = { state.eventSink(HomeScreen.Event.OnContentClicked(content)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "검색",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "저장된 콘텐츠 검색",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    StashTheme {
        Home(
            state = HomeScreen.State(
                contents = listOf(
                    SavedContent.mock,
                    SavedContent(
                        id = "yt-1",
                        title = "유튜브 영상 제목",
                        url = "https://youtube.com/watch?v=123",
                        contentType = ContentType.YOUTUBE,
                        thumbnailUrl = null,
                        description = null,
                        createdAt = Instant.ofEpochMilli(1700000001000L),
                    ),
                ),
                selectedFilter = null,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    StashTheme {
        Home(
            state = HomeScreen.State(
                contents = emptyList(),
                selectedFilter = null,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeFilteredPreview() {
    StashTheme {
        Home(
            state = HomeScreen.State(
                contents = listOf(SavedContent.mock),
                selectedFilter = ContentType.WEB,
            ),
        )
    }
}
